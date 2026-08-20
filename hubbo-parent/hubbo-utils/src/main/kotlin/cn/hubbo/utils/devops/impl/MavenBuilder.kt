package cn.hubbo.utils.devops.impl

import cn.hubbo.utils.devops.config.BuildTool
import java.io.File

/**
 * 基于 Maven 的 [Builder] 实现。
 *
 * 在 [projectDirectory] 下执行 `mvn clean package`，并从 `target/` 收集 JAR/WAR 产物。
 * 与 [JavaDevOpsImpl] 互补，可作为独立能力注入组合装配。
 */
class MavenBuilder(
    projectDirectory: File = File("."),
) : AbstractBuilder(projectDirectory) {

    override val supportedTool: BuildTool = BuildTool.MAVEN

    override val buildCommand: String = "mvn clean package -B -e -U"

    override val outputDirectory: String = "target"

    override val artifactFilter: (File) -> Boolean = { file ->
        file.name.endsWith(".jar") || file.name.endsWith(".war")
    }
}
