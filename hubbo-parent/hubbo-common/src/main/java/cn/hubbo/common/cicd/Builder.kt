package cn.hubbo.common.cicd

import cn.hubbo.common.task.StageTask

/**
 * 构建任务的顶层接口，需要扩展就使用装饰器层层包装，不要使用继承
 */
interface Builder : StageTask, IterationInfo {


}