package cn.hubbo.utils.devops.config

import cn.hubbo.utils.devops.core.Stage
import cn.hubbo.utils.devops.core.model.Environment
import java.time.Duration

/**
 * 流水线级配置（门面级快捷配置）。
 *
 * 完整的声明式流水线应使用 [cn.hubbo.utils.devops.core.model.PipelineDefinition]；
 * 此处提供更简单的键值式配置，供 [cn.hubbo.utils.devops.DevOps] 的快捷入口使用。
 */
data class PipelineOptions(
    val name: String,
    val stages: List<Stage> = Stage.defaultOrder(),
    val branchFilter: String? = null,
    val environment: Environment? = null,
    val parallel: Boolean = false,
    val timeout: Duration = Duration.ofHours(2),
    val retries: Int = 0,
)
