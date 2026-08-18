package cn.hubbo.utils.devops.capability

import cn.hubbo.utils.devops.core.PipelineContext
import cn.hubbo.utils.devops.core.StepResult
import cn.hubbo.utils.devops.core.model.StageDefinition
import cn.hubbo.utils.devops.core.model.StageRunResult
import java.time.Duration
import java.time.Instant

/**
 * 阶段执行器抽象。
 *
 * 默认实现为本地顺序执行（含超时 / 重试）。
 * 分布式实现可将 [StageDefinition] 与 options 序列化后调度到远程节点，
 * 这里只留出扩展接口，不强制实现。
 */
interface StageExecutor {
    suspend fun execute(
        ctx: PipelineContext,
        stage: StageDefinition,
        body: suspend () -> StepResult<*>,
    ): StageRunResult
}

/**
 * 缓存接口。
 *
 * 用于依赖下载缓存、构建缓存等，提升执行效率。
 * 缓存键建议由「阶段 + 输入摘要 + 工具版本」派生。
 */
interface CacheStore {
    suspend fun get(key: String): CacheEntry?
    suspend fun put(key: String, entry: CacheEntry)
    /** 按通配模式失效，如 "build:*"。 */
    suspend fun invalidate(pattern: String)
}

/** 缓存条目。 */
data class CacheEntry(
    val key: String,
    val value: Any?,
    val storedAt: Instant = Instant.now(),
    val ttl: Duration? = null,
) {
    fun isExpired(now: Instant = Instant.now()): Boolean =
        ttl?.let { !now.isBefore(storedAt.plus(it)) } ?: false
}

/** 空实现缓存：命中失败、写入丢弃。 */
object NoopCacheStore : CacheStore {
    override suspend fun get(key: String): CacheEntry? = null
    override suspend fun put(key: String, entry: CacheEntry) {}
    override suspend fun invalidate(pattern: String) {}
}
