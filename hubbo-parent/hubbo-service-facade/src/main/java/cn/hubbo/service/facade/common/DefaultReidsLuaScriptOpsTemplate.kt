package cn.hubbo.service.facade.common

import cn.hubbo.service.common.ReidsLuaScriptOpsTemplate
import jakarta.annotation.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.withContext
import org.springframework.data.redis.connection.ReactiveRedisConnection
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Service
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

@Service
class DefaultReidsLuaScriptOpsTemplate :
    ReidsLuaScriptOpsTemplate {

    @Resource
    lateinit var reactiveRedisTemplate: ReactiveRedisTemplate<String, Any>

    private val connectionFactory: ReactiveRedisConnectionFactory by lazy {
        reactiveRedisTemplate.connectionFactory
    }

    override suspend fun loadScript(scriptContent: String): String {
        return withContext(Dispatchers.IO) {
            return@withContext reactiveRedisTemplate.execute { connection: ReactiveRedisConnection ->
                val scriptingCommands = connection.scriptingCommands()
                val arr = scriptContent.toByteArray(StandardCharsets.UTF_8)
                val byteBuffer = ByteBuffer.allocateDirect(arr.size)
                byteBuffer.put(arr)
                byteBuffer.flip()
                scriptingCommands.scriptLoad(byteBuffer)
            }.awaitLast()
        }
    }

    override suspend fun <T : Any> evalSha(
        sha1: String,
        keys: MutableList<String>,
        argv: MutableList<*>,
        targetType: Class<T>
    ): T {
        return withContext(Dispatchers.IO) {
            val redisScript: RedisScript<T> = object : RedisScript<T> {
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
            return@withContext reactiveRedisTemplate.execute(
                redisScript,
                keys,
                argv
            ).awaitLast()
        }
    }

    override fun getConnection(): ReactiveRedisConnection {
        return connectionFactory.reactiveConnection
    }

}