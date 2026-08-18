package cn.hubbo.utils.devops.extension

import cn.hubbo.utils.devops.core.PipelineContext
import cn.hubbo.utils.devops.core.Stage

/**
 * 钩子触发点。
 *
 * 覆盖流水线生命周期与各能力阶段的前后，用于在特定时刻执行自定义逻辑
 * （如编译前执行脚本、部署后执行健康检查）。
 */
enum class HookPoint {
    PIPELINE_START,
    PIPELINE_END,
    STAGE_START,
    STAGE_END,
    BEFORE_CLONE,
    AFTER_CLONE,
    BEFORE_SYNTAX_CHECK,
    AFTER_SYNTAX_CHECK,
    BEFORE_LINT,
    AFTER_LINT,
    BEFORE_TEST,
    AFTER_TEST,
    BEFORE_STATIC_ANALYSIS,
    AFTER_STATIC_ANALYSIS,
    BEFORE_QUALITY_GATE,
    AFTER_QUALITY_GATE,
    BEFORE_COMPILE,
    AFTER_COMPILE,
    BEFORE_BUILD,
    AFTER_BUILD,
    BEFORE_ARTIFACT_UPLOAD,
    AFTER_ARTIFACT_UPLOAD,
    BEFORE_DEPLOY,
    AFTER_DEPLOY,
    BEFORE_ROLLBACK,
    AFTER_ROLLBACK,
}

/** 钩子载荷。 */
data class HookPayload(
    val hookPoint: HookPoint,
    val stage: Stage? = null,
    val data: Map<String, Any?> = emptyMap(),
)

/**
 * 钩子：在特定阶段前后执行自定义逻辑。
 *
 * 钩子异常应包装为 [cn.hubbo.utils.devops.core.error.DevOpsError]（HOOK_ERROR），
 * 由注册表决定继续 / 中断。
 */
fun interface Hook {
    suspend fun execute(ctx: PipelineContext, payload: HookPayload)
}

/**
 * 钩子注册表。
 *
 * 按触发点注册 / 注销，并按注册顺序（[order]）执行；
 * 实现必须线程安全，支持并行流水线各自持有独立的注册表。
 */
interface HookRegistry {
    fun register(hookPoint: HookPoint, hook: Hook, name: String? = null, order: Int = 0)

    fun unregister(name: String): Boolean

    suspend fun fire(
        ctx: PipelineContext,
        hookPoint: HookPoint,
        payload: HookPayload = HookPayload(hookPoint),
    )
}

/** 空实现注册表：不注册任何钩子，[fire] 为空操作。 */
object NoopHookRegistry : HookRegistry {
    override fun register(hookPoint: HookPoint, hook: Hook, name: String?, order: Int) {}
    override fun unregister(name: String): Boolean = false
    override suspend fun fire(ctx: PipelineContext, hookPoint: HookPoint, payload: HookPayload) {}
}
