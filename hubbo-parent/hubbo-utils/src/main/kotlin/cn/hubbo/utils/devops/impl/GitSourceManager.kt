package cn.hubbo.utils.devops.impl

import cn.hubbo.utils.devops.capability.SourceManager
import cn.hubbo.utils.devops.config.AuthSpec
import cn.hubbo.utils.devops.config.CloneOptions
import cn.hubbo.utils.devops.config.RevisionSpec
import cn.hubbo.utils.devops.core.PipelineContext
import cn.hubbo.utils.devops.core.Stage
import cn.hubbo.utils.devops.core.error.DevOpsError
import cn.hubbo.utils.devops.core.error.ErrorCode
import cn.hubbo.utils.devops.core.model.CheckoutResult
import cn.hubbo.utils.devops.core.model.Revision
import cn.hubbo.utils.devops.core.model.RevisionType
import cn.hubbo.utils.devops.core.model.VcsType
import kotlinx.coroutines.*
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.InvalidConfigurationException
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.errors.TransportException
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.ProgressMonitor
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.*
import org.eclipse.jgit.transport.sshd.IdentityPasswordProvider
import org.eclipse.jgit.transport.sshd.SshdSessionFactory
import org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.util.io.DisabledOutputStream
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.time.Clock
import java.time.Instant
import kotlin.coroutines.coroutineContext

/**
 * 基于 JGit 的生产级 Git 源码管理实现。
 *
 * 覆盖 [SourceManager] 的克隆、切换、修订解析与清理四个能力。
 *
 * ## 设计决策
 * - **手动 clone（init + fetch + checkout）而非 [Git.cloneRepository]**：
 *   `CloneCommand` 在 fetch 之前无法写入本地仓库配置（如 HTTP 代理），
 *   手动流程可以在发起任何网络 I/O 前注入代理、认证与 SSH 会话工厂，
 *   对「认证 + 代理 + 指定修订」的组合场景行为完全可控。
 * - **认证**：匿名 / Token / 用户名密码走 `CredentialsProvider`（HTTP Basic）；
 *   SSH 私钥走 `SshdSessionFactoryBuilder`，显式注入私钥文件路径与口令，不依赖 `~/.ssh` 默认密钥。
 * - **代理**：在内存中配置 `http.proxy` / `http.proxyUser` / `http.proxyPassword`（不落盘），
 *   `TransportHttp` 在建立连接时从同一 Repository 实例读取，无需全局静态配置（线程安全）。
 * - **取消**：所有 JGit 调用在 [Dispatchers.IO] 上执行，并通过
 *   [CancellationAwareProgressMonitor] 将协程取消传播给 JGit（不留下孤儿进程）。
 * - **错误映射**：JGit 异常统一翻译为 [DevOpsError]，区分可恢复（网络、超时）
 *   与致命（认证、配置、修订不存在）错误，供编排器决策重试/终止。
 * - **线程安全**：本类无状态（仅持有 [clock] 与 [dispatcher]），每次调用创建并关闭
 *   独立的 `Repository`，可安全地由多个流水线并发使用。
 * - **可测试**：构造参数可注入 [Clock] 与 [CoroutineDispatcher]；单元测试可基于
 *   本地 `file://` 仓库做端到端验证，无需外网。
 */
open class GitSourceManager(
    private val clock: Clock = Clock.systemUTC(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SourceManager {

    override val supportedVcs: Set<VcsType> = setOf(VcsType.GIT)

    private val logger: Logger = LoggerFactory.getLogger(GitSourceManager::class.java)

    /** fetch 拉取的默认引用集合：所有远端分支 + 所有标签 + 远端 HEAD（用于识别默认分支）。 */
    private val defaultRefSpecs = arrayOf(
        RefSpec("+refs/heads/*:refs/remotes/origin/*"),
        RefSpec("+refs/tags/*:refs/tags/*"),
        RefSpec("+HEAD:refs/remotes/origin/HEAD"),
    )

    // ==================== 对外能力 ====================

    override suspend fun clone(ctx: PipelineContext, options: CloneOptions): CheckoutResult = ctx.trace("git.clone") {
        validateVcs(options)
        val workspace = resolveWorkspace(options, ctx)
        clean(ctx, workspace)
        ensureWorkspaceAvailable(workspace)
        logger.info(
            "git clone {} -> {} (revision={})",
            redactUrl(options.repositoryUrl), workspace, options.revision
        )

        withContext(dispatcher) {
            val monitor = cancellationMonitor()
            gitOperation(Stage.CLONE, "clone") {
                coroutineContext.ensureActive()
                val git = Git.init().setDirectory(workspace.toFile()).call()
                try {
                    configureRepository(git.repository, options)
                    fetchRefs(git, options, monitor, *defaultRefSpecs)
                    coroutineContext.ensureActive()
                    val revision = resolveRevisionInternal(git.repository, options.revision)
                    checkoutRevision(git, revision)
                    updateSubmodulesIfRequested(git, options)
                    val result = buildCheckoutResult(git.repository, workspace, revision)
                    logger.info(
                        "git clone done: {} @ {} ({})",
                        redactUrl(options.repositoryUrl), result.revision.commitHash, result.revision.ref
                    )
                    result
                } finally {
                    git.close()
                }
            }
        }
    }

    override suspend fun checkout(ctx: PipelineContext, options: CloneOptions, revision: Revision): CheckoutResult =
        ctx.trace("git.checkout") {
            validateVcs(options)
            val workspace = resolveWorkspace(options, ctx)
            withContext(dispatcher) {
                val monitor = cancellationMonitor()
                gitOperation(Stage.CLONE, "checkout") {
                    val git = openGit(workspace)
                    try {
                        coroutineContext.ensureActive()
                        // 若目标修订本地不存在，则按需拉取，随后再解析。
                        ensureRevisionAvailable(git, options, revision, monitor)
                        coroutineContext.ensureActive()
                        val oldHead = git.repository.resolve("HEAD")
                        val resolved = resolveRevisionInternal(git.repository, toSpec(revision))
                        checkoutRevision(git, resolved)
                        updateSubmodulesIfRequested(git, options)
                        buildCheckoutResult(git.repository, workspace, resolved, oldHead)
                    } finally {
                        git.close()
                    }
                }
            }
        }

    override suspend fun resolveRevision(ctx: PipelineContext, options: CloneOptions, spec: RevisionSpec): Revision =
        ctx.trace("git.resolve-revision") {
            validateVcs(options)
            val workspace = resolveWorkspace(options, ctx)
            withContext(dispatcher) {
                gitOperation(Stage.CLONE, "resolve-revision") {
                    val git = openGit(workspace)
                    try {
                        resolveRevisionInternal(git.repository, spec)
                    } finally {
                        git.close()
                    }
                }
            }
        }

    override suspend fun clean(ctx: PipelineContext, workspace: Path): Unit = ctx.trace("git.clean") {
        withContext(dispatcher) {
            gitOperation(Stage.CLONE, "clean") {
                if (!Files.exists(workspace)) {
                    logger.debug("git clean: workspace not present, skip: {}", workspace)
                    return@gitOperation
                }
                logger.info("git clean {}", workspace)
                // commons-io 能处理 Windows 下的只读文件（git 对象文件常为只读）。
                FileUtils.deleteDirectory(workspace.toFile())
            }
        }
    }

    // ==================== 内部实现 ====================

    /** 校验 VCS 类型，避免误用（如对 SVN URL 调用本实现）。 */
    private fun validateVcs(options: CloneOptions) {
        if (options.vcs != VcsType.GIT && options.vcs != VcsType.NONE) {
            throw DevOpsError.fatal(
                ErrorCode.UNSUPPORTED_FEATURE,
                Stage.CLONE,
                "GitSourceManager only supports GIT, got ${options.vcs}",
            )
        }
    }

    /** 目标工作区：优先 [CloneOptions.targetDirectory]，否则工作目录下按仓库名推断。 */
    private fun resolveWorkspace(options: CloneOptions, ctx: PipelineContext): Path =
        options.targetDirectory ?: ctx.workingDirectory.resolve(repoName(options.repositoryUrl))

    private fun repoName(url: String): String {
        val name = url.trimEnd('/').substringAfterLast('/').removeSuffix(".git")
        return name.ifBlank { "repository" }
    }

    /** 目标目录必须不存在或为空，避免静默覆盖已有数据。 */
    private fun ensureWorkspaceAvailable(workspace: Path) {
        if (Files.exists(workspace)) {
            val nonEmpty = Files.list(workspace).use { it.findAny().isPresent }
            if (nonEmpty) {
                throw DevOpsError.fatal(
                    ErrorCode.CLONE_FAILED,
                    Stage.CLONE,
                    "target directory is not empty: $workspace, call clean() first",
                )
            }
        }
    }

    /** 打开已有仓库（要求目录下存在 .git）。 */
    private fun openGit(workspace: Path): Git {
        val gitDir = workspace.resolve(".git").toFile()
        if (!gitDir.isDirectory) {
            throw DevOpsError.fatal(
                ErrorCode.CLONE_FAILED,
                Stage.CLONE,
                "not a git repository: $workspace",
            )
        }
        val repo = FileRepositoryBuilder().setGitDir(gitDir).readEnvironment().build()
        return Git.wrap(repo)
    }

    /** 在发起网络 I/O 前配置仓库（当前用于 HTTP 代理）。 */
    private fun configureRepository(repo: Repository, options: CloneOptions) {
        val proxy = options.proxy ?: return
        val config = repo.config
        config.setString("http", null, "proxy", "${proxy.scheme}://${proxy.host}:${proxy.port}")
        if (!proxy.username.isNullOrBlank()) config.setString("http", null, "proxyUser", proxy.username)
        if (!proxy.password.isNullOrBlank()) config.setString("http", null, "proxyPassword", proxy.password)
        // 只写内存配置、不调用 config.save()：代理密码明文落盘到 .git/config 存在泄露风险。
        // TransportHttp 通过同一个 Repository 实例读取该配置，无需持久化即可生效。
    }

    /** 按需拉取指定引用集合。 */
    private fun fetchRefs(
        git: Git,
        options: CloneOptions,
        monitor: ProgressMonitor?,
        vararg refSpecs: RefSpec,
    ) {
        val fetch = git.fetch()
            .setRemote(options.repositoryUrl)
            .setRefSpecs(*refSpecs)
            .setCredentialsProvider(credentialsProvider(options.auth))
            .setTransportConfigCallback { transport -> configureTransport(transport, options) }
        if (monitor != null) fetch.setProgressMonitor(monitor)
        val depth = options.depth
        if (depth != null && depth > 0) fetch.setDepth(depth)
        fetch.call()
    }

    /**
     * 确保修订版本本地可用；缺失时按需拉取。
     * 分支/标签拉取具体引用；提交哈希拉取全部引用后再次解析（哈希可及性取决于服务端配置）。
     */
    private fun ensureRevisionAvailable(
        git: Git,
        options: CloneOptions,
        revision: Revision,
        monitor: ProgressMonitor?
    ) {
        val repo = git.repository
        when (revision.type) {
            RevisionType.BRANCH -> {
                val local = repo.findRef("refs/heads/${revision.ref}")
                val remote = repo.findRef("refs/remotes/origin/${revision.ref}")
                if (local == null && remote == null) {
                    fetchRefs(
                        git,
                        options,
                        monitor,
                        RefSpec("+refs/heads/${revision.ref}:refs/remotes/origin/${revision.ref}")
                    )
                }
            }

            RevisionType.TAG -> {
                if (repo.exactRef("refs/tags/${revision.ref}") == null) {
                    fetchRefs(git, options, monitor, RefSpec("+refs/tags/${revision.ref}:refs/tags/${revision.ref}"))
                }
            }

            RevisionType.COMMIT -> {
                if (repo.resolve(revision.commitHash ?: revision.ref) == null) {
                    fetchRefs(git, options, monitor, *defaultRefSpecs)
                }
            }
        }
    }

    /** 认证凭据：仅 HTTP(S) 传输使用；SSH 走 [SshdSessionFactory]。 */
    private fun credentialsProvider(auth: AuthSpec): CredentialsProvider? = when (auth) {
        AuthSpec.Anonymous -> null
        is AuthSpec.Token -> UsernamePasswordCredentialsProvider(TOKEN_USERNAME, auth.token)
        is AuthSpec.UsernamePassword -> UsernamePasswordCredentialsProvider(auth.username, auth.password)
        is AuthSpec.SshKey -> null
    }

    /**
     * 传输配置回调：为 SSH 传输注入会话工厂。
     * Token 类认证以 Basic 形式发送（GitHub 使用 `x-access-token` 用户名），
     * [AuthSpec.Token.tokenHeader] 预留用于未来基于 Header 的自定义传输。
     */
    private fun configureTransport(transport: Transport, options: CloneOptions) {
        val sshAuth = options.auth as? AuthSpec.SshKey ?: return
        if (transport is SshTransport) {
            transport.sshSessionFactory = createSshFactory(sshAuth)
        }
    }

    /**
     * 构建 [SshdSessionFactory]。
     *
     * 通过 [SshdSessionFactoryBuilder] 注入私钥文件路径（setDefaultIdentities），
     * 加密私钥的口令由 [IdentityPasswordProvider] 提供；
     * 已知主机文件默认使用 `$HOME/.ssh/known_hosts`，可通过 [AuthSpec.SshKey.knownHostsPath]
     * 的父目录作为 ssh 目录。
     */
    private fun createSshFactory(auth: AuthSpec.SshKey): SshdSessionFactory {
        val homeDir = File(System.getProperty("user.home"))
        val sshDir = auth.knownHostsPath?.let { File(it).parentFile } ?: File(homeDir, ".ssh")
        return SshdSessionFactoryBuilder()
            .setHomeDirectory(homeDir)
            .setSshDirectory(sshDir)
            .setPreferredAuthentications("publickey")
            .setDefaultIdentities { listOf(Path.of(auth.privateKeyPath)) }
            .setKeyPasswordProvider { cp ->
                auth.passphrase?.let { passphrase ->
                    object : IdentityPasswordProvider(cp) {
                        override fun getPassword(uri: URIish?, message: String?): CharArray = passphrase.toCharArray()
                    }
                }
            }
            .build(null)
    }

    /** 将修订版本解析为本地可用的 [Revision]（引用必须已存在）。 */
    private fun resolveRevisionInternal(repo: Repository, spec: RevisionSpec): Revision = when (spec) {
        is RevisionSpec.Branch -> resolveBranch(repo, spec.name)
        is RevisionSpec.Tag -> resolveTag(repo, spec.name)
        is RevisionSpec.Commit -> resolveCommit(repo, spec.hash)
        RevisionSpec.Default -> resolveHead(repo)
    }

    private fun resolveBranch(repo: Repository, name: String): Revision {
        val ref = repo.findRef("refs/heads/$name")
            ?: repo.findRef("refs/remotes/origin/$name")
            ?: throw revisionNotFound("branch '$name'")
        val objectId = ref.objectId ?: throw revisionNotFound("branch '$name'")
        return Revision(name, RevisionType.BRANCH, objectId.name)
    }

    private fun resolveTag(repo: Repository, name: String): Revision {
        val ref = repo.exactRef("refs/tags/$name") ?: throw revisionNotFound("tag '$name'")
        val objectId = ref.objectId ?: throw revisionNotFound("tag '$name'")
        return Revision(name, RevisionType.TAG, objectId.name)
    }

    private fun resolveCommit(repo: Repository, hash: String): Revision {
        val objectId = repo.resolve(hash) ?: throw revisionNotFound("commit '$hash'")
        return Revision(hash, RevisionType.COMMIT, objectId.name)
    }

    /**
     * 解析默认修订：优先本地 HEAD。
     *
     * 关键点：仓库处于分离 HEAD（检出标签/提交）时，`exactRef("HEAD")` 返回的是直接引用，
     * 其 [Ref.name] 就是 "HEAD"。若按分支名返回会产生非法的分支名 "HEAD"，
     * 导致 checkout 时报 `Branch name HEAD is not allowed`。
     * 因此分离 HEAD 一律以 [RevisionType.COMMIT] 形式返回。
     */
    private fun resolveHead(repo: Repository): Revision {
        val head = repo.exactRef("HEAD") ?: throw revisionNotFound("HEAD")
        val objectId = head.objectId
            ?: run {
                // 初始/悬空 HEAD（尚未检出）：回退到远端默认分支。
                return resolveBranch(repo, defaultRemoteBranch(repo))
            }
        return if (head.isSymbolic) {
            // 符号引用：refs/heads/<branch>
            val branch = head.target.name.substringAfterLast('/')
            Revision(branch, RevisionType.BRANCH, objectId.name)
        } else {
            // 分离 HEAD：以提交形式表示当前检出点。
            Revision(objectId.name, RevisionType.COMMIT, objectId.name)
        }
    }

    /**
     * 推导远端默认分支名。
     *
     * `refs/remotes/origin/HEAD` 可能被 JGit 存储为：
     * - 符号引用（`symref -> refs/remotes/origin/main`）：直接取目标分支名；
     * - 直接引用（被解析到提交）：此时 [Ref.target] 就是它自身，无法从名字推断分支，
     *   回退为扫描 refs/remotes/origin 前缀下的唯一分支；多个分支时保守回退 `main`。
     */
    private fun defaultRemoteBranch(repo: Repository): String {
        val originHead = repo.exactRef("refs/remotes/origin/HEAD")
        val symbolicTarget = originHead?.takeIf { it.isSymbolic }?.target?.name
        if (symbolicTarget != null) {
            val branch = symbolicTarget.removePrefix("refs/remotes/origin/")
            if (branch.isNotBlank() && branch != "HEAD") return branch
        }
        val branches = repo.refDatabase.getRefsByPrefix("refs/remotes/origin/")
            .map { it.name.removePrefix("refs/remotes/origin/") }
            .filter { it.isNotBlank() && !it.equals("HEAD", ignoreCase = true) }
            .distinct()
        return branches.singleOrNull() ?: "main"
    }

    /** 检出修订版本：分支建本地跟踪分支，标签/提交为分离 HEAD。 */
    private fun checkoutRevision(git: Git, revision: Revision) {
        val repo = git.repository
        when (revision.type) {
            RevisionType.BRANCH -> {
                // "HEAD" 是保留名，git 不允许以其作为分支名；给出可读错误而非 JGit 的 InvalidRefNameException。
                if (revision.ref.equals("HEAD", ignoreCase = true)) {
                    throw DevOpsError.fatal(
                        ErrorCode.REVISION_NOT_FOUND,
                        Stage.CLONE,
                        "branch name '${revision.ref}' is reserved; use a concrete branch name or commit hash",
                    )
                }
                val localRef = "refs/heads/${revision.ref}"
                if (repo.exactRef(localRef) == null) {
                    git.branchCreate()
                        .setName(revision.ref)
                        .setStartPoint("refs/remotes/origin/${revision.ref}")
                        .setForce(false)
                        .call()
                }
                git.checkout().setName(revision.ref).call()
            }

            RevisionType.TAG -> git.checkout().setName("refs/tags/${revision.ref}").call()
            RevisionType.COMMIT -> git.checkout().setName(revision.commitHash ?: revision.ref).call()
        }
    }

    /** 需要子模块时初始化并更新（失败会随 clone/checkout 一起失败）。 */
    private fun updateSubmodulesIfRequested(git: Git, options: CloneOptions) {
        if (!options.recursiveSubmodules) return
        val submoduleUpdate = git.submoduleUpdate()
            .setCredentialsProvider(credentialsProvider(options.auth))
        submoduleUpdate.setTransportConfigCallback { transport -> configureTransport(transport, options) }
        git.submoduleInit().call()
        submoduleUpdate.call()
    }

    /** 组装 [CheckoutResult]，附带 HEAD 提交信息；切换场景计算与旧 HEAD 的差异文件。 */
    private fun buildCheckoutResult(
        repo: Repository,
        workspace: Path,
        revision: Revision,
        oldHead: ObjectId? = null,
    ): CheckoutResult {
        val head = repo.resolve("HEAD")
        val commit = head?.let { repo.parseCommit(it) }
        val changedFiles = if (oldHead != null && head != null && oldHead != head) {
            changedFilesBetween(repo, oldHead, head)
        } else {
            emptyList()
        }
        return CheckoutResult(
            workspace = workspace,
            revision = revision.copy(commitHash = head?.name ?: revision.commitHash),
            vcs = VcsType.GIT,
            changedFiles = changedFiles,
            commitMessage = commit?.shortMessage,
            author = commit?.authorIdent?.name,
            clonedAt = Instant.now(clock),
        )
    }

    /** 计算两个提交之间的变更文件列表（用于流水线条件触发）。 */
    private fun changedFilesBetween(repo: Repository, from: ObjectId, to: ObjectId): List<String> {
        repo.newObjectReader().use { reader ->
            DiffFormatter(DisabledOutputStream.INSTANCE).use { formatter ->
                formatter.setRepository(repo)
                val oldTree = CanonicalTreeParser().also { it.reset(reader, from) }
                val newTree = CanonicalTreeParser().also { it.reset(reader, to) }
                return formatter.scan(oldTree, newTree)
                    .mapNotNull { it.newPath ?: it.oldPath }
                    .distinct()
            }
        }
    }

    private fun toSpec(revision: Revision): RevisionSpec = when (revision.type) {
        RevisionType.BRANCH -> RevisionSpec.Branch(revision.ref)
        RevisionType.TAG -> RevisionSpec.Tag(revision.ref)
        RevisionType.COMMIT -> RevisionSpec.Commit(revision.commitHash ?: revision.ref)
    }

    private fun revisionNotFound(detail: String): DevOpsError =
        DevOpsError.fatal(ErrorCode.REVISION_NOT_FOUND, Stage.CLONE, "git revision not found: $detail")

    /** 协程取消 → JGit 取消：JGit 的 ProgressMonitor 周期性查询取消标志。 */
    private suspend fun cancellationMonitor(): ProgressMonitor? {
        val job = coroutineContext[Job] ?: return null
        return CancellationAwareProgressMonitor { !job.isCancelled }
    }

    /**
     * JGit 异常 → [DevOpsError] 统一映射。
     * 顺序敏感：RepositoryNotFoundException 是 IOException 子类，须先于 IOException 捕获。
     */
    private inline fun <T> gitOperation(stage: Stage, operation: String, block: () -> T): T {
        return try {
            block()
        } catch (e: DevOpsError) {
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: InvalidConfigurationException) {
            throw DevOpsError.fatal(
                ErrorCode.CONFIGURATION_INVALID,
                stage,
                "invalid git configuration during $operation",
                e
            )
        } catch (e: TransportException) {
            if (isAuthFailure(e)) {
                throw DevOpsError.fatal(
                    ErrorCode.AUTH_FAILED,
                    stage,
                    "git authentication failed during $operation: ${e.message}",
                    e
                )
            }
            throw DevOpsError.recoverable(ErrorCode.NETWORK_ERROR, stage, "git $operation failed: ${e.message}", e)
        } catch (e: RepositoryNotFoundException) {
            throw DevOpsError.recoverable(
                ErrorCode.CLONE_FAILED,
                stage,
                "git repository not found during $operation",
                e
            )
        } catch (e: IOException) {
            throw DevOpsError.recoverable(ErrorCode.CLONE_FAILED, stage, "git $operation I/O error: ${e.message}", e)
        } catch (e: Exception) {
            throw DevOpsError.recoverable(ErrorCode.UNKNOWN, stage, "git $operation failed: ${e.message}", e)
        }
    }

    /** 通过异常消息启发式判断是否为认证失败（JGit 无独立 AuthException）。 */
    private fun isAuthFailure(e: TransportException): Boolean {
        val message = e.message?.lowercase() ?: return false
        return AUTH_FAILURE_HINTS.any { message.contains(it) }
    }

    /** 从 URL 中剥离 userinfo，避免在日志中泄露凭据。 */
    private fun redactUrl(url: String): String {
        val schemeEnd = url.indexOf("://")
        if (schemeEnd == -1) return url
        val rest = url.substring(schemeEnd + 3)
        val at = rest.indexOf('@')
        return if (at >= 0) url.substring(0, schemeEnd + 3) + rest.substring(at + 1) else url
    }

    /** 协程取消感知的进度监视器。 */
    private class CancellationAwareProgressMonitor(
        private val isActive: () -> Boolean,
    ) : ProgressMonitor {
        override fun start(totalTasks: Int) {}
        override fun beginTask(title: String, totalWork: Int) {}
        override fun update(completed: Int) {}
        override fun endTask() {}
        override fun showDuration(show: Boolean) {}
        override fun isCancelled(): Boolean = !isActive()
    }

    companion object {
        /** 匿名 / Token 走 HTTP Basic 时使用的用户名（GitHub PAT 约定，其它托管商多可忽略）。 */
        private const val TOKEN_USERNAME = "x-access-token"

        /** 认证失败常见的异常消息特征，用于将 [TransportException] 归类为认证失败。 */
        private val AUTH_FAILURE_HINTS = listOf(
            "auth", "authentication", "permission denied", "publickey", "401", "403", "not authorized",
        )

        @JvmStatic
        fun create(): GitSourceManager = GitSourceManager()
    }
}
