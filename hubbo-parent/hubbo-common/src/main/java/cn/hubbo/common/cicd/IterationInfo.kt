package cn.hubbo.common.cicd

interface IterationInfo {

    /**
     * 获取当前迭代信息，迭代只能往前推荐，不可回退
     */
    fun currentIteration(): Iteration


}