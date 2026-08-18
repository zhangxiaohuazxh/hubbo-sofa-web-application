package cn.hubbo.utils.devops.core.model

import cn.hubbo.utils.devops.core.Stage
import cn.hubbo.utils.devops.core.StepResult
import cn.hubbo.utils.devops.core.StepStatus
import java.time.Duration
import java.time.Instant

/**
 * 流水线定义（声明式）。
 *
 * 与执行解耦：编排器读取定义中的阶段、依赖、并行、条件、超时与重试策略来驱动执行。
 * 便于从 YAML / JSON 反序列化，也便于分布式执行（将定义序列化后调度到远程节点）。
 */
data class PipelineDefinition(
    val name: String,
    val version: String = "1",
    val stages: List<StageDefinition>,
    val globalTimeout: Duration? = null,
    val onSuccessHooks: List<String> = emptyList(),
    val onFailureHooks: List<String> = emptyList(),
)

/** 单个阶段的定义。 */
data class StageDefinition(
    val name: String,
    val stage: Stage,
    /** 依赖的其他阶段名，用于 DAG 编排。 */
    val dependsOn: Set<String> = emptySet(),
    val runParallel: Boolean = false,
    /** 条件触发表达式，如 "branch == 'main'"，由编排器/表达式引擎求值。 */
    val condition: String? = null,
    val timeout: Duration = Duration.ofMinutes(10),
    val retries: Int = 0,
    /** 阶段级配置（对应各能力 Options 的字段映射）。 */
    val options: Map<String, Any?> = emptyMap(),
    val hooksBefore: List<String> = emptyList(),
    val hooksAfter: List<String> = emptyList(),
)

/** 流水线运行状态。 */
enum class PipelineStatus { PENDING, RUNNING, PAUSED, SUCCEEDED, FAILED, CANCELLED, SKIPPED }

/** 单个阶段的运行结果。 */
data class StageRunResult(
    val stageName: String,
    val stage: Stage,
    val status: StepStatus,
    val startedAt: Instant,
    val duration: Duration = Duration.ZERO,
    val result: StepResult<*> = StepResult.Skipped(),
    val retryCount: Int = 0,
)

/** 一次流水线运行。 */
data class PipelineRun(
    val runId: String,
    val definition: PipelineDefinition,
    val status: PipelineStatus,
    val stageResults: List<StageRunResult> = emptyList(),
    val startedAt: Instant,
    val finishedAt: Instant? = null,
    val currentStage: String? = null,
)
