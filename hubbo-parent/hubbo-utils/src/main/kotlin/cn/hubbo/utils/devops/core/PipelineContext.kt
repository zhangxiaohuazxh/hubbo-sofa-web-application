package cn.hubbo.utils.devops.core

import cn.hubbo.utils.devops.capability.MetricsSink
import cn.hubbo.utils.devops.capability.NoopMetrics
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 贯穿整条流水线的上下文对象，等价于 Go 语言中的 context.Context。
 *
 * 设计决策：
 * - 所有能力方法均以 [PipelineContext] 作为第一个参数，携带取消/超时语义（[coroutineContext]）、
 *   全局配置、日志、指标与阶段信息；
 * - [attributes] 为并发安全的运行级共享状态，供同一运行内多个并行阶段读写；
 * - [withStage] 派生新上下文进入下一阶段，未变化的部分自动透传；
 * - [trace] 为统一的计时与指标埋点入口，实现可观测性。
 */
interface PipelineContext {
    /** 结构化并发上下文，供实现做 withContext / withTimeout / 取消传播。 */
    val coroutineContext: CoroutineContext

    val pipelineName: String
    val runId: String
    val stage: Stage
    val logger: Logger
    val metrics: MetricsSink

    /** 全局配置（key-value），不鼓励放入敏感信息，密钥应走 [EnvironmentManager]。 */
    val config: Map<String, String>
    val workingDirectory: Path

    /** 运行级共享状态（线程安全）。 */
    val attributes: MutableMap<String, Any?>

    /** 派生进入指定阶段的上下文。 */
    fun withStage(stage: Stage): PipelineContext

    /** 带计时与指标埋点的执行包装。 */
    suspend fun <T> trace(name: String, block: suspend () -> T): T {
        val started = System.nanoTime()
        logger.debug("[{}] step start: {}", stage, name)
        return try {
            block().also {
                val ms = (System.nanoTime() - started) / 1_000_000
                logger.debug("[{}] step done: {} in {}ms", stage, name, ms)
                metrics.histogram(
                    "devops.step.duration",
                    ms.toDouble(),
                    mapOf("stage" to stage.name, "step" to name),
                )
            }
        } catch (t: Throwable) {
            metrics.counter("devops.step.failure", tags = mapOf("stage" to stage.name, "step" to name))
            throw t
        }
    }
}

/** [PipelineContext] 的默认不可变实现。 */
class DefaultPipelineContext(
    override val coroutineContext: CoroutineContext,
    override val pipelineName: String,
    override val runId: String,
    override val stage: Stage,
    override val logger: Logger,
    override val metrics: MetricsSink,
    override val config: Map<String, String>,
    override val workingDirectory: Path,
) : PipelineContext {

    override val attributes: MutableMap<String, Any?> = ConcurrentHashMap()

    override fun withStage(stage: Stage): PipelineContext = DefaultPipelineContext(
        coroutineContext = coroutineContext,
        pipelineName = pipelineName,
        runId = runId,
        stage = stage,
        logger = logger,
        metrics = metrics,
        config = config,
        workingDirectory = workingDirectory,
    )
}

/** [PipelineContext] 便捷工厂。 */
object PipelineContexts {

    fun default(
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
        pipelineName: String = "default",
        runId: String = UUID.randomUUID().toString(),
        stage: Stage = Stage.CLONE,
        logger: Logger = LoggerFactory.getLogger("DevOps.Pipeline"),
        metrics: MetricsSink = NoopMetrics,
        config: Map<String, String> = emptyMap(),
        workingDirectory: Path = Paths.get(System.getProperty("java.io.tmpdir")),
    ): PipelineContext = DefaultPipelineContext(
        coroutineContext = coroutineContext,
        pipelineName = pipelineName,
        runId = runId,
        stage = stage,
        logger = logger,
        metrics = metrics,
        config = config,
        workingDirectory = workingDirectory,
    )
}
