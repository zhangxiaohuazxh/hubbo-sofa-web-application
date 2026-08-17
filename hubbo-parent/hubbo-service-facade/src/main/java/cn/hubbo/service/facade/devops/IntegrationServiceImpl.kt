package cn.hubbo.service.facade.devops

import cn.hubbo.dal.IntegrationDao
import cn.hubbo.entity.vo.IterationVO
import cn.hubbo.service.devops.IntegrationService
import cn.hubbo.utils.CommandLineUtils
import jakarta.annotation.Resource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class IntegrationServiceImpl : IntegrationService {

    private val logger: Logger by lazy { LoggerFactory.getLogger(IntegrationServiceImpl::class.java) }

    @Resource
    private lateinit var iterationDao: IntegrationDao


    override suspend fun continuousIntegration(iteration: IterationVO) {
        logger.info("===================开始执行部署任务===================")
        logger.info("项目信息 {}", iteration)
        val projectIteration = iterationDao.findProjectIntegrationInfoByIterationId(iteration.iterationId)
        logger.info("查询到的迭代信息 {}", projectIteration)
        val project = iterationDao.findProjectInfoByProjectId(projectIteration!!.projectId)
        logger.info("查询到的项目信息  {}", project)
        //  执行阶段任务
        //  clone
        //  checkout
        //   compile
        //   test
        //   build
        val command =
            "rm -rf ${project!!.projectName} && git clone ${project!!.repositoryUrl} && cd ${project.projectName}  && git checkout ${projectIteration.currentBranch} && mvn clean compile package"
        val res = CommandLineUtils.exec(command, timeoutMillis = 60_000L)
        logger.info("执行结果 {}", res)
        logger.info("===================部署任务执行完成===================")
    }

    override suspend fun continuousDelivery() {
        TODO("Not yet implemented")
    }


}