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
import java.nio.file.Path
import java.security.MessageDigest
import java.time.Duration

/**
 * 基于命令行构建工具的 [Builder] 抽象基类。
 *
 * 提供统一的构建执行、产物收集与校验和计算模板，子类只需声明构建命令与产物目录。
 */
abstract class AbstractBuilder(
    protected val projectDirectory: File = File("."),
) : Builder {

    override val supportedTools: Set<BuildTool> = setOf(supportedTool)

    /** 当前构建器对应的构建工具。 */
    protected abstract val supportedTool: BuildTool

    /** 构建命令（在项目根目录下执行）。 */
    protected abstract val buildCommand: String

    /** 产物输出目录，相对于 [projectDirectory]。 */
    protected abstract val outputDirectory: String

    /** 产物文件匹配规则，默认匹配所有文件。 */
    protected open val artifactFilter: (File) -> Boolean = { true }

    override suspend fun build(ctx: PipelineContext, options: BuildOptions): BuildResult {
        if (!projectDirectory.exists() || !projectDirectory.isDirectory) {
            throw DevOpsError.recoverable(
                ErrorCode.BUILD_FAILED,
                Stage.BUILD,
                "项目目录不存在或不是目录: ${projectDirectory.absolutePath}"
            )
        }

        val result = CommandLineUtils.execute(buildCommand, workingDirectory = projectDirectory)
        if (result.exitCode != 0) {
            throw DevOpsError.recoverable(
                ErrorCode.BUILD_FAILED,
                Stage.BUILD,
                result.output ?: "${supportedTool.name.lowercase()} build failed"
            )
        }

        val artifacts = collectArtifacts(options)
        return BuildResult(artifacts = artifacts, duration = Duration.ZERO)
    }

    /**
     * 收集构建产物并计算元数据。
     */
    protected open fun collectArtifacts(options: BuildOptions): List<Artifact> {
        val targetDir = File(projectDirectory, outputDirectory)
        if (!targetDir.isDirectory) {
            return emptyList()
        }

        return targetDir.walkTopDown()
            .filter { it.isFile && artifactFilter(it) }
            .map { file ->
                val path = file.toPath()
                Artifact(
                    name = file.name,
                    type = options.artifactType,
                    file = path,
                    checksum = calculateSha256(path),
                    sizeBytes = Files.size(path),
                )
            }
            .toList()
    }

    /**
     * 计算文件的 SHA-256 校验和。
     */
    protected fun calculateSha256(path: Path): String {
        val digest = MessageDigest.getInstance("SHA-256")
        Files.newInputStream(path).use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return "sha256:" + digest.digest().joinToString("") { "%02x".format(it) }
    }
}
