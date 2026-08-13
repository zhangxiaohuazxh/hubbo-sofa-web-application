package cn.hubbo.entity.devops.ci

class BuildStage {

    var name: String? = null

    /* 要执行的命令 */
    var run: String? = null

    var command: String? = run

    override fun toString(): String =
        "BuildStage{name='$name', run='$run' command='$run'}"
}
