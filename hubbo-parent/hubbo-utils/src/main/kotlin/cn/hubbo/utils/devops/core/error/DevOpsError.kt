package cn.hubbo.utils.devops.core.error

import cn.hubbo.utils.devops.core.Stage
import java.time.Duration

/**
 * 统一异常类型。
 *
 * 包含错误码（[code]）、阶段标识（[stage]）、可恢复性（[recoverability]）
 * 以及结构化上下文（[context]）。
 *
 * 设计决策：
 * - 继承 [RuntimeException]，便于直接抛出并穿透非 suspend 边界（如回调、线程池）；
 * - [context] 记录阶段相关键值（目标 URL、产物路径、工具版本等），用于日志与告警模板；
 * - 通过 companion 工厂方法快速构造「可恢复」与「致命」错误。
 */
class DevOpsError(
    val code: ErrorCode,
    val stage: Stage,
    message: String,
    val recoverability: Recoverability,
    cause: Throwable? = null,
    val context: Map<String, String> = emptyMap(),
) : RuntimeException(message, cause) {

    companion object {
        fun recoverable(
            code: ErrorCode,
            stage: Stage,
            message: String,
            cause: Throwable? = null,
            context: Map<String, String> = emptyMap(),
        ): DevOpsError = DevOpsError(code, stage, message, Recoverability.RECOVERABLE, cause, context)

        fun fatal(
            code: ErrorCode,
            stage: Stage,
            message: String,
            cause: Throwable? = null,
            context: Map<String, String> = emptyMap(),
        ): DevOpsError = DevOpsError(code, stage, message, Recoverability.FATAL, cause, context)

        /** 将任意 [Throwable] 包装为 [DevOpsError]；本身已是 [DevOpsError] 时原样返回。 */
        fun fromThrowable(
            stage: Stage,
            t: Throwable,
            context: Map<String, String> = emptyMap(),
        ): DevOpsError = if (t is DevOpsError) {
            t
        } else {
            DevOpsError(
                code = ErrorCode.UNKNOWN,
                stage = stage,
                message = t.message ?: t.javaClass.simpleName,
                recoverability = Recoverability.RECOVERABLE,
                cause = t,
                context = context,
            )
        }
    }
}

/**
 * 常用错误构造助手，供各实现按统一模板生成错误，保证告警文案一致。
 */
object Errors {

    fun invalidConfig(stage: Stage, detail: String): DevOpsError =
        DevOpsError.fatal(ErrorCode.CONFIGURATION_INVALID, stage, detail)

    fun authFailed(stage: Stage, detail: String): DevOpsError =
        DevOpsError.fatal(ErrorCode.AUTH_FAILED, stage, detail)

    fun toolNotFound(stage: Stage, tool: String): DevOpsError =
        DevOpsError.fatal(ErrorCode.TOOL_NOT_FOUND, stage, "required tool not found: $tool")

    fun timedOut(stage: Stage, timeout: Duration): DevOpsError =
        DevOpsError.recoverable(ErrorCode.TIMEOUT, stage, "operation timed out after $timeout")

    fun cancelled(stage: Stage): DevOpsError =
        DevOpsError.fatal(ErrorCode.CANCELLED, stage, "operation cancelled")

    fun unsupported(stage: Stage, feature: String): DevOpsError =
        DevOpsError.fatal(ErrorCode.UNSUPPORTED_FEATURE, stage, "feature not supported: $feature")
}
