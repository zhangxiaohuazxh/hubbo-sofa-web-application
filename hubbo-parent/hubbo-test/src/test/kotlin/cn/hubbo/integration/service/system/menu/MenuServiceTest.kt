package cn.hubbo.integration.service.system.menu

import cn.hubbo.dal.MenuDao
import cn.hubbo.integration.SofaApplicationTest
import jakarta.annotation.Resource
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MenuServiceTest : SofaApplicationTest() {

    @Resource
    private lateinit var menuDao: MenuDao

    private val logger: Logger by lazy { LoggerFactory.getLogger(MenuServiceTest::class.java) }

    @Test
    fun testQueryAllMenus(): Unit = runBlocking {
        ::menuDao.isInitialized.takeIf { it }?.let {
            val menus = menuDao.findAllMenus()
            logger.info("查询到的菜单信息 {}", menus)
        }
    }


}