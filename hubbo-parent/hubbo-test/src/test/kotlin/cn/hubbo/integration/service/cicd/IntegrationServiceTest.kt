package cn.hubbo.integration.service.cicd

import cn.hubbo.entity.vo.IterationVO
import cn.hubbo.integration.SofaApplicationTest
import cn.hubbo.service.devops.IntegrationService
import jakarta.annotation.Resource
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class IntegrationServiceTest : SofaApplicationTest() {

    private val logger: Logger by lazy { LoggerFactory.getLogger(IntegrationServiceTest::class.java) }

    @Resource
    private lateinit var integrationService: IntegrationService

    @Test
    fun deployTest(): Unit = runBlocking {
        ::integrationService.isInitialized.takeIf { it }?.let {
            logger.info("测试任务开始执行")
            integrationService.continuousIntegration(IterationVO(1L, "测试迭代"))
            logger.info("测试任务执行结束")
        }
    }


}