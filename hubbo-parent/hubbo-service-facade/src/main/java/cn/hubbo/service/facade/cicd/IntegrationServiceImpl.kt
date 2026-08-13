package cn.hubbo.service.facade.cicd

import cn.hubbo.common.cicd.Project
import cn.hubbo.service.cicd.IntegrationService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class IntegrationServiceImpl : IntegrationService {

    private val logger: Logger by lazy { LoggerFactory.getLogger(IntegrationServiceImpl::class.java) }

    override fun deploy(project: Project) {
        logger.info("===================开始执行部署任务===================")
        logger.info("项目信息 {}", project)
        logger.info("===================部署任务执行完成===================")
    }


}