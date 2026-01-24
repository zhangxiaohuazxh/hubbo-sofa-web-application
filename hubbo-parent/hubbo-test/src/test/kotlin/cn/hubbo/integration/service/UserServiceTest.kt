package cn.hubbo.integration.service

import cn.hubbo.config.db.DbProperties
import jakarta.annotation.Resource
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Mono

@SpringBootTest
class UserServiceTest {

    @Resource
    private lateinit var context: DSLContext

    @Resource
    private lateinit var properties: DbProperties

    private val logger: Logger by lazy { LoggerFactory.getLogger(UserServiceTest::class.java) }

    @Test
    fun testQueryCurrentTimestamp(): Unit = runBlocking {
        logger.info("开始执行查询")
        ::context.isInitialized.takeIf { it }?.let {
            logger.info("context初始化成功")
            val res = Mono.from(context.select(DSL.currentTimestamp()))
                .map { it.into(String::class.java) }
                .awaitSingleOrNull()
            logger.info("查询结果 {}", res)
        } ?: run {
            logger.error("依赖注入失败")
        }
    }

}