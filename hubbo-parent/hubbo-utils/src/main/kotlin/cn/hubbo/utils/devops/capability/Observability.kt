package cn.hubbo.utils.devops.capability

import cn.hubbo.utils.devops.core.PipelineContext

/**
 * 指标接口。
 *
 * 计数器 / 计量器 / 直方图，可对接 Prometheus（micrometer-registry-prometheus）。
 * 实现必须线程安全（同一名称的并发写入由实现自行合并）。
 */
interface MetricsSink {
    fun counter(name: String, delta: Long = 1, tags: Map<String, String> = emptyMap())
    fun gauge(name: String, value: Double, tags: Map<String, String> = emptyMap())
    fun histogram(name: String, value: Double, tags: Map<String, String> = emptyMap())
}

/**
 * 步骤追踪。
 *
 * 记录每个步骤的耗时与状态，输出统一的观测日志 / 指标。
 */
interface StepTracer {
    suspend fun <T> trace(ctx: PipelineContext, name: String, block: suspend () -> T): T
}

/** 空实现指标，便于测试与未配置场景（静默丢弃）。 */
object NoopMetrics : MetricsSink {
    override fun counter(name: String, delta: Long, tags: Map<String, String>) {}
    override fun gauge(name: String, value: Double, tags: Map<String, String>) {}
    override fun histogram(name: String, value: Double, tags: Map<String, String>) {}
}
