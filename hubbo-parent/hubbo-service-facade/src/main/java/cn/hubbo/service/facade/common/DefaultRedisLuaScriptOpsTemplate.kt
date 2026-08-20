package cn.hubbo.service.facade.common

import cn.hubbo.service.common.RedisLuaScriptOpsTemplate
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.util.ReferenceCountUtil
import jakarta.annotation.Resource
import kotlinx.coroutines.reactive.awaitLast
import org.springframework.data.redis.connection.ReactiveRedisConnection
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets

@Service
class DefaultRedisLuaScriptOpsTemplate :
    RedisLuaScriptOpsTemplate {

    @Resource
    lateinit var reactiveRedisTemplate: ReactiveRedisTemplate<String, Any>

    private val connectionFactory: ReactiveRedisConnectionFactory by lazy {
        reactiveRedisTemplate.connectionFactory
    }

    /** 按 sha1+结果类型缓存脚本实例，有界缓存，避免无上限增长 */
    private val scriptCache: Cache<String, RedisScript<*>> = CacheBuilder.newBuilder()
        .maximumSize(512)
        .build()

    override suspend fun loadScript(scriptContent: String): String {
        return reactiveRedisTemplate.execute { connection: ReactiveRedisConnection ->
            var byteBuf: ByteBuf? = null
            try {
                val arr = scriptContent.toByteArray(StandardCharsets.UTF_8)
                byteBuf = ByteBufAllocator.DEFAULT.directBuffer(arr.size)
                val scriptingCommands = connection.scriptingCommands()
                byteBuf.writeBytes(arr)
                scriptingCommands.scriptLoad(byteBuf.nioBuffer())
            } finally {
                byteBuf?.let {
                    ReferenceCountUtil.release(byteBuf)
                }
            }
        }.awaitLast()
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : Any> evalSha(
        sha1: String,
        keys: MutableList<String>,
        argv: MutableList<*>,
        targetType: Class<T>
    ): T {
        val redisScript: RedisScript<T> = scriptCache.get("$sha1:${targetType.name}") {
            object : RedisScript<T> {
                override fun getSha1(): String {
                    return sha1
                }

                override fun getResultType(): Class<T> {
                    return targetType
                }

                override fun getScriptAsString(): String {
                    return ""
                }
            }
        } as RedisScript<T>
        return reactiveRedisTemplate.execute(
            redisScript,
            keys,
            argv
        ).awaitLast()
    }

    override fun getConnection(): ReactiveRedisConnection {
        return connectionFactory.reactiveConnection
    }

}