package cn.hubbo.utils.devops.core

import cn.hubbo.utils.devops.core.error.DevOpsError
import cn.hubbo.utils.devops.core.error.ErrorCode

/**
 * 每一步能力的执行结果。
 *
 * 设计决策：使用 sealed interface 而非「成功返回、失败抛异常」的二元模型，
 * 因为流水线中存在大量「预期内的失败」（如门禁不通过、测试失败），
 * 结构化表达便于上层编排器做分支判断、报告聚合与告警。
 */
sealed interface StepResult<out T> {
    data class Success<T>(val value: T) : StepResult<T>
    data class Failure(val error: DevOpsError) : StepResult<Nothing>
    data class Skipped(val reason: String? = null) : StepResult<Nothing>

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure
    val isSkipped: Boolean get() = this is Skipped

    fun getOrNull(): T? = (this as? Success)?.value

    /** 成功时返回值，失败/跳过时抛出对应异常。 */
    fun getOrThrow(): T = when (this) {
        is Success -> value
        is Failure -> throw error
        is Skipped -> throw DevOpsError.fatal(
            ErrorCode.SKIPPED,
            Stage.REPORT,
            "step skipped: ${reason ?: "no reason"}",
        )
    }

    fun <R> map(transform: (T) -> R): StepResult<R> = when (this) {
        is Success -> StepResult.Success(transform(value))
        is Failure -> this
        is Skipped -> this
    }
}
