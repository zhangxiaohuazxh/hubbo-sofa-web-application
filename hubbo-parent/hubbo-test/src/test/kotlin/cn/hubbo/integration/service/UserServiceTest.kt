package cn.hubbo.integration.service

import cn.hubbo.config.db.DbProperties
import cn.hubbo.dal.UserDao
import jakarta.annotation.Resource
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Mono

@Disabled
@SpringBootTest
class UserServiceTest {

    @Resource
    private lateinit var context: DSLContext

    @Resource
    private lateinit var properties: DbProperties

    @Resource
    private lateinit var userDao: UserDao

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

    @Test
    fun testQueryUserByUsernameLike(): Unit = runBlocking {
        ::userDao.isInitialized.takeIf { it }?.let {
            val res = userDao.findByUsernameLike("a")
            logger.info("testQueryUserByUsername查询到的用户信息 {}", res)
        } ?: run {
            logger.info("testQueryUserByUsername初始化失败")
        }
    }

    @Test
    fun testQueryUserByUsernameEquals(): Unit = runBlocking {
        ::userDao.isInitialized.takeIf { it }?.let {
            val res = userDao.findByUsernameEquals("test")
            logger.info("testQueryUserByUsernameEquals查询到的用户信息 {}", res)
        } ?: run {
            logger.info("testQueryUserByUsernameEquals初始化失败")
        }
    }

    @Test
    fun testMany2ManyQuery() = runBlocking {
        assert(::userDao.isInitialized)
        val userInfos = userDao.findUserRolesByUserId(1769524839L)
        logger.info("testMany2ManyQuery查询到的用户角色信息 {}", userInfos)
    }

    fun testMutltiSet() = runBlocking {
        assert(::userDao.isInitialized)

    }


}