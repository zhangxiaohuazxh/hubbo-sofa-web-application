package cn.hubbo.utils.devops.config

import cn.hubbo.utils.devops.core.model.VcsType
import java.nio.file.Path

/**
 * 认证规格。
 *
 * sealed interface 保证穷举；新增认证方式时新增子类即可，无需修改调用方。
 */
sealed interface AuthSpec {
    data object Anonymous : AuthSpec
    data class SshKey(
        val privateKeyPath: String,
        val passphrase: String? = null,
        val knownHostsPath: String? = null,
    ) : AuthSpec

    data class Token(
        val token: String,
        val tokenHeader: String = "Authorization",
    ) : AuthSpec

    data class UsernamePassword(
        val username: String,
        val password: String,
    ) : AuthSpec
}

/** 代理配置。 */
data class ProxySpec(
    val host: String,
    val port: Int,
    val scheme: String = "http",
    val username: String? = null,
    val password: String? = null,
)

/** 待解析的修订版本规格（分支 / 标签 / 提交哈希 / 默认分支）。 */
sealed interface RevisionSpec {
    data class Branch(val name: String) : RevisionSpec
    data class Tag(val name: String) : RevisionSpec
    data class Commit(val hash: String) : RevisionSpec
    data object Default : RevisionSpec
}

/**
 * 代码克隆配置。
 *
 * 覆盖 VCS 选择、修订版本、认证、代理、浅克隆与子模块等常用场景。
 */
data class CloneOptions(
    val repositoryUrl: String,
    val vcs: VcsType = VcsType.GIT,
    val revision: RevisionSpec = RevisionSpec.Default,
    val targetDirectory: Path? = null,
    val auth: AuthSpec = AuthSpec.Anonymous,
    val proxy: ProxySpec? = null,
    /** 浅克隆深度（git --depth）。 */
    val depth: Int? = null,
    val recursiveSubmodules: Boolean = false,
    val refspec: String? = null,
)
