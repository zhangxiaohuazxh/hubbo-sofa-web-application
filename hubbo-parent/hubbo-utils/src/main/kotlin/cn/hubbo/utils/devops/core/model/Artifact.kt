package cn.hubbo.utils.devops.core.model

import cn.hubbo.utils.devops.config.AuthSpec
import cn.hubbo.utils.devops.config.Platform
import java.nio.file.Path
import java.time.Duration

/** 交付物类型。 */
enum class ArtifactType {
    JAR,
    WAR,
    DOCKER_IMAGE,
    ZIP,
    TAR_GZ,
    EXECUTABLE,
    NPM_PACKAGE,
    PYTHON_WHEEL,
    DEB,
    RPM,
    GENERIC,
}

/** 一次构建/下载得到的产物。 */
data class Artifact(
    val name: String,
    val type: ArtifactType,
    val file: Path,
    val checksum: String,
    val sizeBytes: Long,
    val buildMetadata: Map<String, String> = emptyMap(),
    /** 制品坐标（上传/下载时用于定位与生成存储 key）；缺省时回退解析 [buildMetadata]。 */
    val coordinates: Coordinates? = null,
)

/** 制品坐标（兼容 Maven 坐标系）。 */
data class Coordinates(
    val group: String,
    val artifact: String,
    val version: String,
    val packaging: String = "jar",
    val classifier: String? = null,
) {
    val coordinateKey: String get() = "$group:$artifact:$version:$packaging${classifier?.let { ":$it" } ?: ""}"
}

/** 制品仓库中的引用（上传后返回，用于后续下载/晋升/回滚）。 */
data class ArtifactReference(
    val coordinates: Coordinates,
    val repository: String,
    val storageKey: String,
    val metadata: Map<String, String> = emptyMap(),
    /** 存储桶（S3/GCS 等对象存储使用）；缺省时按 [repository] 从实现配置解析。 */
    val bucket: String? = null,
    /** 对象版本号（开启版本控制的桶返回；未开启时为 null）。 */
    val versionId: String? = null,
)

/** 制品仓库类型。 */
enum class RepositoryType { NEXUS, JFROG, DOCKER_REGISTRY, S3, GCS, GENERIC_HTTP }

/** 制品仓库目标（上传目的地）。 */
data class ArtifactRepository(
    val name: String,
    val type: RepositoryType,
    val url: String,
    val credentials: AuthSpec = AuthSpec.Anonymous,
)

/** 编译结果。 */
data class CompileResult(
    val outputs: List<Path>,
    val warnings: List<String> = emptyList(),
    val duration: Duration,
    val platform: Platform? = null,
    val incrementalCacheHit: Boolean = false,
)

/** 构建结果。 */
data class BuildResult(
    val artifacts: List<Artifact>,
    val dockerImageReference: String? = null,
    val duration: Duration,
    val cacheHit: Boolean = false,
)
