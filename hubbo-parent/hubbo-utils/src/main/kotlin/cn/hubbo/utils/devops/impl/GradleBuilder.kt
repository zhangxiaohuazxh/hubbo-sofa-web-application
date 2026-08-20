package cn.hubbo.utils.devops.impl

import cn.hubbo.utils.devops.config.BuildTool
import java.io.File

/**
 * 基于 Gradle 的 [Builder] 实现。
 *
 * 在 [projectDirectory] 下执行 `./gradlew clean build`，并从 `build/libs` 收集 JAR 产物。
 * 支持 Gradle Wrapper，无需本地安装 Gradle。
 */
class GradleBuilder(
    projectDirectory: File = File("."),
) : AbstractBuilder(projectDirectory) {

    override val supportedTool: BuildTool = BuildTool.GRADLE

    override val buildCommand: String = "./gradlew clean build"

    override val outputDirectory: String = "build/libs"

    override val artifactFilter: (File) -> Boolean = { file ->
        file.name.endsWith(".jar")
    }
}
