package cn.hubbo.service.cicd

import cn.hubbo.common.cicd.Project

interface IntegrationService {


    fun deploy(project: Project);


}
