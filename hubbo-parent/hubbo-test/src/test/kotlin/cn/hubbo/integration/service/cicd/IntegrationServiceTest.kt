package cn.hubbo.integration.service.cicd

import cn.hubbo.common.cicd.Project
import cn.hubbo.integration.SofaApplicationTest
import cn.hubbo.service.cicd.IntegrationService
import org.junit.jupiter.api.Test
import jakarta.annotation.Resource
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class IntegrationServiceTest : SofaApplicationTest() {

    private val logger: Logger by lazy { LoggerFactory.getLogger(IntegrationServiceTest::class.java) }

    @Resource
    private lateinit var integrationService: IntegrationService

    @Test
    fun deployTest() {
        ::integrationService.isInitialized.takeIf { it }?.let {
            logger.info("测试任务开始执行")
            integrationService.deploy(Project(1L))
            logger.info("测试任务执行结束")
        }
    }


}