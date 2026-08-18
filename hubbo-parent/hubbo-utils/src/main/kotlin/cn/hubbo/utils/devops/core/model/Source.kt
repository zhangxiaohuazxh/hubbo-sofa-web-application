package cn.hubbo.utils.devops.core.model

import java.nio.file.Path
import java.time.Instant

/** 版本控制系统类型。 */
enum class VcsType { GIT, SVN, MERCURIAL, NONE }

/** 修订版本类型。 */
enum class RevisionType { BRANCH, TAG, COMMIT }

/**
 * 已解析的修订版本。
 *
 * [ref] 为展示用引用（分支名 / 标签 / 哈希），
 * [commitHash] 为精确解析后的提交哈希（来源管理实现负责解析填充）。
 */
data class Revision(
    val ref: String,
    val type: RevisionType,
    val commitHash: String? = null,
)

/** 代码拉取结果。 */
data class CheckoutResult(
    val workspace: Path,
    val revision: Revision,
    val vcs: VcsType,
    val changedFiles: List<String> = emptyList(),
    val commitMessage: String? = null,
    val author: String? = null,
    val clonedAt: Instant = Instant.now(),
)

/** 源语言类型，用于语法检查工具路由与语言相关配置。 */
enum class Language {
    KOTLIN,
    JAVA,
    GO,
    RUST,
    PYTHON,
    JAVASCRIPT,
    TYPESCRIPT,
    C,
    CPP,
    RUBY,
    PHP,
    SHELL,
    SQL,
    DOCKERFILE,
    NONE,
}
