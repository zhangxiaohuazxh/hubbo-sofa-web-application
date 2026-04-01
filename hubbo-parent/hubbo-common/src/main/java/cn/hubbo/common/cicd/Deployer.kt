package cn.hubbo.common.cicd

import cn.hubbo.common.task.StageTask

/**
 * 部署的顶层接口，同样使用装饰器扩展新的功能
 */
interface Deployer : StageTask, IterationInfo {


}