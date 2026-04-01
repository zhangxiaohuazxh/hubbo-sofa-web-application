package cn.hubbo.common.cicd

import java.time.LocalDateTime

/**
 * 迭代信息，一个项目下可以有多个并行迭代，也支持抢占迭代，如prod下可以有多个迭代，但是服务器上只能同时运行一个迭代，
 * 如线上运行的是迭代a，迭代b就可以直接部署挤掉迭代a，迭代b开始部署
 */
data class Iteration(
    /* 迭代id */
    val id: Long,
    /* 迭代名称 */
    val iterationName: String,
    /* 创建人 */
    val creator: Long,
    /* 创建人姓名 */
    val creatorName: String,
    /* 创建时间 */
    val createTime: LocalDateTime,
    /* 迭代当前的状态 */
    val status: IterationStatus,
    /* 仓库地址 */
    val repositoryUrl: String,
    /* 当前迭代的分支 */
    val branch: String,
    val project: Project
)

