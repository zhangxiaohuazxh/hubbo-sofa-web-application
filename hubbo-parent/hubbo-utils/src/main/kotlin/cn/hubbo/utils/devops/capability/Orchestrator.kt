package cn.hubbo.utils.devops.capability

import cn.hubbo.utils.devops.core.PipelineContext
import cn.hubbo.utils.devops.core.model.PipelineDefinition
import cn.hubbo.utils.devops.core.model.PipelineRun

/**
 * 流水线编排能力。
 *
 * 定义各阶段执行顺序、并行策略、条件触发（如仅在 main 分支触发部署），
 * 支持暂停、重试与超时控制。
 *
 * 说明：编排器通过 [PipelineDefinition] 驱动执行；分布式实现可将定义与
 * 阶段选项序列化后调度到远程节点，本接口只暴露编排语义，不绑定执行位置。
 */
interface PipelineOrchestrator {
    suspend fun run(ctx: PipelineContext, pipeline: PipelineDefinition): PipelineRun

    suspend fun cancel(ctx: PipelineContext, runId: String)

    /** 从指定阶段（含）起重试；[fromStage] 为空时整条流水线重跑。 */
    suspend fun retry(ctx: PipelineContext, runId: String, fromStage: String? = null): PipelineRun

    suspend fun pause(ctx: PipelineContext, runId: String)

    suspend fun resume(ctx: PipelineContext, runId: String)

    fun status(ctx: PipelineContext, runId: String): PipelineRun?

    /** 订阅流水线事件。 */
    fun attach(observer: PipelineObserver): CancellableSubscription
}

/** 流水线事件类型。 */
enum class PipelineEvent {
    STAGE_STARTED,
    STAGE_FINISHED,
    PIPELINE_FINISHED,
    PIPELINE_FAILED,
    PIPELINE_CANCELLED,
    PIPELINE_PAUSED,
    PIPELINE_RESUMED,
}

/** 流水线观察者：监听阶段与整条流水线事件（用于日志、通知、指标）。 */
fun interface PipelineObserver {
    fun onEvent(ctx: PipelineContext, run: PipelineRun, event: PipelineEvent)
}

/** 可取消的订阅句柄。 */
fun interface CancellableSubscription {
    fun cancel()
}
