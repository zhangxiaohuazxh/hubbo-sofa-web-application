package cn.hubbo.service.devops

import cn.hubbo.entity.vo.IterationVO

interface IntegrationService {

    /**
     * 持续集成，自动化构建和测试
     *
     * iteration 迭代信息
     */
    suspend fun continuousIntegration(iteration: IterationVO)

    /**
     * 自动部署
     */
    suspend fun continuousDelivery()

}
