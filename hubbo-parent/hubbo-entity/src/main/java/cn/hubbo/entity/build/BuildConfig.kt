package cn.hubbo.entity.build

class BuildConfig {

    /* 构建的任务名称 */
    var name: String? = null

    /* 构建任务下的阶段任务 */
    var steps: List<BuildStage>? = null

    override fun toString(): String =
        "BuildConfig{name='$name', steps=$steps}"
}
