package cn.hubbo.unit.devops

import cn.hubbo.utils.devops.config.CloneOptions
import cn.hubbo.utils.devops.config.RevisionSpec
import cn.hubbo.utils.devops.core.PipelineContexts
import cn.hubbo.utils.devops.core.Stage
import cn.hubbo.utils.devops.core.model.RevisionType
import cn.hubbo.utils.devops.impl.GitSourceManager
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.Git
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/**
 * [GitSourceManager] 回归测试。
 *
 * 使用本地 `file://` 仓库，无需外网；覆盖「分离 HEAD 下解析默认修订」这一曾导致
 * `Branch name HEAD is not allowed` 的场景。
 *
 * 说明：JGit 在 Windows 上可能短暂持有 pack 文件句柄，无法用 JUnit `@TempDir` 自动清理，
 * 因此使用手工临时目录 + 尽力清理（清理失败仅告警，交由系统回收）。
 */
class GitSourceManagerTest {

    private val logger: Logger = LoggerFactory.getLogger(GitSourceManagerTest::class.java)

    @Test
    fun `resolveHead on detached checkout returns a commit revision, not branch HEAD`(): Unit = runBlocking {
        val tempBase = Files.createTempDirectory("git-sm-test")
        try {
            val sourceDir = tempBase.resolve("source")
            Files.createDirectories(sourceDir)
            setupSourceRepo(sourceDir)

            val cloneDir = tempBase.resolve("clone")
            val git = GitSourceManager()
            val ctx = PipelineContexts.default(pipelineName = "test", stage = Stage.CLONE)
            val url = sourceDir.toUri().toString()

            // 1. 克隆默认分支：revision 类型应为 BRANCH，ref 不应为 "HEAD"
            val cloned = git.clone(ctx, CloneOptions(repositoryUrl = url, targetDirectory = cloneDir))
            assertEquals(RevisionType.BRANCH, cloned.revision.type)
            assertNotEquals("HEAD", cloned.revision.ref)

            // 2. 检出标签 -> 进入分离 HEAD 状态
            val opts = CloneOptions(repositoryUrl = url, targetDirectory = cloneDir)
            val tagRev = git.resolveRevision(ctx, opts, RevisionSpec.Tag("v1.0.0"))
            git.checkout(ctx, opts, tagRev)

            // 3. 分离 HEAD 下解析默认修订：必须返回 COMMIT，而不是分支名 "HEAD"
            val headRev = git.resolveRevision(ctx, opts, RevisionSpec.Default)
            assertEquals(RevisionType.COMMIT, headRev.type)
            assertNotEquals("HEAD", headRev.ref)

            // 4. 基于解析结果再次切换，不应抛出 "Branch name HEAD is not allowed"
            git.checkout(ctx, opts, headRev)
        } finally {
            bestEffortCleanup(tempBase)
        }
    }

    /** 在指定目录初始化一个含一次提交与一个轻量标签的 git 仓库。 */
    private fun setupSourceRepo(dir: Path) {
        val sourceGit = Git.init().setDirectory(dir.toFile()).call()
        try {
            Files.writeString(dir.resolve("README.md"), "hello")
            sourceGit.add().addFilepattern(".").call()
            sourceGit.commit().setMessage("init").call()
            sourceGit.tag().setName("v1.0.0").call()
        } finally {
            sourceGit.close()
        }
    }

    /** 尽力清理：JGit 文件锁（Windows）可能导致删除失败，仅记录告警。 */
    private fun bestEffortCleanup(dir: Path) {
        try {
            FileUtils.deleteDirectory(dir.toFile())
        } catch (e: IOException) {
            logger.warn("无法清理临时目录（可能被 JGit 文件锁占用），交由系统清理: {}", dir, e)
        }
    }
}
