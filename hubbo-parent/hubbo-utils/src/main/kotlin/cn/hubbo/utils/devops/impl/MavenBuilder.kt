package cn.hubbo.utils.devops.impl

import cn.hubbo.utils.CommandLineUtils
import cn.hubbo.utils.devops.capability.Builder
import cn.hubbo.utils.devops.config.BuildOptions
import cn.hubbo.utils.devops.config.BuildTool
import cn.hubbo.utils.devops.core.PipelineContext
import cn.hubbo.utils.devops.core.Stage
import cn.hubbo.utils.devops.core.error.DevOpsError
import cn.hubbo.utils.devops.core.error.ErrorCode
import cn.hubbo.utils.devops.core.model.Artifact
import cn.hubbo.utils.devops.core.model.BuildResult
import java.io.File
import java.nio.file.Files
import java.time.Duration

/**
 * 基于 Maven 的 [Builder] 实现。
 *
 * 在 [projectDirectory] 下执行 `mvn package`，并从 `target/` 收集 JAR 产物。
 * 用于 Java 项目的构建能力；与 [JavaDevOpsImpl] 互补，可作为独立能力注入组合装配。
 */
class MavenBuilder(
    private val projectDirectory: File = File("."),
) : Builder {

    override val supportedTools: Set<BuildTool> = setOf(BuildTool.MAVEN)

    override suspend fun build(ctx: PipelineContext, options: BuildOptions): BuildResult {
        val result = CommandLineUtils.execute("mvn clean compile package -B -e -U", workingDirectory = projectDirectory)
        if (result.exitCode != 0) {
            throw DevOpsError.recoverable(ErrorCode.BUILD_FAILED, Stage.BUILD, result.output ?: "mvn package failed")
        }
        val targetDir = File(projectDirectory, "target")
        val jars = targetDir.takeIf { it.isDirectory }
            ?.listFiles { file -> file.isFile && file.name.endsWith(".jar") }
            ?.toList()
            ?: emptyList()
        val artifacts = jars.map { file ->
            val path = file.toPath()
            Artifact(
                name = file.name,
                type = options.artifactType,
                file = path,
                checksum = "sha256-mock",
                sizeBytes = Files.size(path),
            )
        }
        return BuildResult(artifacts = artifacts, duration = Duration.ZERO)
    }
}
