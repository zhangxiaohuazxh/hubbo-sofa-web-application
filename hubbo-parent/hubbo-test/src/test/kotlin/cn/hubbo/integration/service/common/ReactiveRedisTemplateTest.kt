package cn.hubbo.integration.service.common

import cn.hubbo.integration.SofaApplicationTest
import cn.hubbo.service.common.ReidsLuaScriptOpsTemplate
import jakarta.annotation.Resource
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ReactiveRedisTemplateTest : SofaApplicationTest() {

    private val logger: Logger by lazy { LoggerFactory.getLogger(ReactiveRedisTemplateTest::class.java) }

    @Resource
    private lateinit var redisScriptOpsTemplate: ReidsLuaScriptOpsTemplate

    @Disabled
    @Test
    fun testUploadScript2RedisServer(): Unit = runBlocking {
        ::redisScriptOpsTemplate.isInitialized.takeIf { it }?.let {
            val scriptContent =
                "redis.call('SET',KEYS[1],ARGV[1]) return redis.call('GET',KEYS[1])"
            val sha = redisScriptOpsTemplate.loadScript(scriptContent)
            logger.info("上传脚本的执行结果 {}", sha)
            val res = redisScriptOpsTemplate.evalSha(
                sha,
                mutableListOf("a"),
                mutableListOf<Any>("1"),
                String::class.java
            )
            logger.info("脚本执行结果 {}", res)
        } ?: run {
            logger.error("依赖注入失败")
        }

    }


}