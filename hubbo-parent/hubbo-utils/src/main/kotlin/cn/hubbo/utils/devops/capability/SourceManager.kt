package cn.hubbo.utils.devops.capability

import cn.hubbo.utils.devops.config.CloneOptions
import cn.hubbo.utils.devops.config.RevisionSpec
import cn.hubbo.utils.devops.core.PipelineContext
import cn.hubbo.utils.devops.core.model.CheckoutResult
import cn.hubbo.utils.devops.core.model.Revision
import cn.hubbo.utils.devops.core.model.VcsType
import java.nio.file.Path

/**
 * 源码管理能力。
 *
 * 负责从多种 VCS（Git / SVN / Mercurial）拉取代码，
 * 支持指定分支 / 标签 / 提交哈希、认证（SSH / Token / 用户名密码）与代理配置。
 *
 * 约定：
 * - 实现必须线程安全，支持并行拉取多个仓库；
 * - [supportedVcs] 暴露能力范围，调用方据此做特性检测。
 */
interface SourceManager {
    /** 该实现支持的 VCS 类型。 */
    val supportedVcs: Set<VcsType>

    /** 按 [CloneOptions] 拉取代码到工作区。 */
    suspend fun clone(ctx: PipelineContext, options: CloneOptions): CheckoutResult

    /** 在工作区中切换到指定修订版本（用于流水线中途切换分支）。 */
    suspend fun checkout(ctx: PipelineContext, options: CloneOptions, revision: Revision): CheckoutResult

    /** 将分支 / 标签 / 哈希字符串解析为精确修订版本。 */
    suspend fun resolveRevision(ctx: PipelineContext, options: CloneOptions, spec: RevisionSpec): Revision

    /** 清理本地工作区（释放磁盘 / 避免脏状态）。 */
    suspend fun clean(ctx: PipelineContext, workspace: Path)
}
