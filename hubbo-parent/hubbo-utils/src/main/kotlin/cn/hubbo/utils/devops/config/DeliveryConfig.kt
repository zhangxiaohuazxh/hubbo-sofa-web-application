package cn.hubbo.utils.devops.config

import cn.hubbo.utils.devops.core.model.ArtifactReference
import cn.hubbo.utils.devops.core.model.ArtifactType
import cn.hubbo.utils.devops.core.model.DeployStrategy
import cn.hubbo.utils.devops.core.model.Environment
import java.nio.file.Path
import java.time.Duration

/** 目标平台（用于交叉编译与多架构构建）。 */
data class Platform(
    val os: String,
    val arch: String,
    val libc: String? = null,
) {
    companion object {
        val LINUX_AMD64 = Platform("linux", "amd64")
        val LINUX_ARM64 = Platform("linux", "arm64")
        val WINDOWS_AMD64 = Platform("windows", "amd64")
        val DARWIN_AMD64 = Platform("darwin", "amd64")
    }
}

/** 编译配置。 */
data class CompileOptions(
    val outputDirectory: Path? = null,
    /** 优化级别（0 ~ 3）。 */
    val optimizationLevel: Int = 0,
    /** 宏定义，如 ["FEATURE_X", "1"]。 */
    val macros: Map<String, String> = emptyMap(),
    val targetPlatform: Platform? = null,
    val additionalArgs: List<String> = emptyList(),
    val incremental: Boolean = true,
    val timeout: Duration = Duration.ofMinutes(15),
)

/** 构建工具。 */
enum class BuildTool { MAVEN, GRADLE, MAKE, DOCKER, NPM, GO, CARGO, PIP, GENERIC }

/** Docker 镜像构建配置。 */
data class DockerBuildOptions(
    val dockerfile: Path,
    val imageName: String,
    val tags: List<String> = emptyList(),
    val buildArgs: Map<String, String> = emptyMap(),
    val buildContext: Path? = null,
    val cacheFrom: List<String> = emptyList(),
    val platform: Platform? = null,
)

/** 构建配置。 */
data class BuildOptions(
    val tool: BuildTool,
    val artifactType: ArtifactType,
    val parameters: Map<String, String> = emptyMap(),
    val docker: DockerBuildOptions? = null,
    val outputDirectory: Path? = null,
    val timeout: Duration = Duration.ofMinutes(30),
)

/** 金丝雀部署参数。 */
data class CanaryOptions(
    val initialWeight: Int = 10,
    val steps: Int = 5,
    val stepInterval: Duration = Duration.ofMinutes(1),
)

/** 健康检查参数。 */
data class HealthCheckOptions(
    val endpoint: String? = null,
    val expectedStatus: Int = 200,
    val timeout: Duration = Duration.ofMinutes(5),
    val retries: Int = 3,
)

/** 部署配置。 */
data class DeployOptions(
    val environment: Environment,
    val strategy: DeployStrategy = DeployStrategy.ROLLING,
    val artifactReference: ArtifactReference? = null,
    val targetUrl: String? = null,
    val configOverride: Map<String, String> = emptyMap(),
    val canary: CanaryOptions? = null,
    val healthCheck: HealthCheckOptions? = null,
    val timeout: Duration = Duration.ofMinutes(30),
)
