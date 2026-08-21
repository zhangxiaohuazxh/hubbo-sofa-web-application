package cn.hubbo.utils.devops.impl

import cn.hubbo.utils.devops.capability.ArtifactManager
import cn.hubbo.utils.devops.core.PipelineContext
import cn.hubbo.utils.devops.core.Stage
import cn.hubbo.utils.devops.core.error.DevOpsError
import cn.hubbo.utils.devops.core.error.ErrorCode
import cn.hubbo.utils.devops.core.model.Artifact
import cn.hubbo.utils.devops.core.model.ArtifactReference
import cn.hubbo.utils.devops.core.model.ArtifactRepository
import cn.hubbo.utils.devops.core.model.ArtifactType
import cn.hubbo.utils.devops.core.model.Coordinates
import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.CopyObjectRequest
import aws.sdk.kotlin.services.s3.model.DeleteObjectRequest
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.ListObjectVersionsRequest
import aws.sdk.kotlin.services.s3.model.MetadataDirective
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.sdk.kotlin.services.s3.model.S3Exception
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.content.fromFile
import aws.smithy.kotlin.runtime.content.writeToFile
import aws.smithy.kotlin.runtime.http.engine.AlpnId
import aws.smithy.kotlin.runtime.http.engine.okhttp.OkHttpEngine
import aws.smithy.kotlin.runtime.net.url.Url
import kotlin.time.Duration.Companion.seconds
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

/**
 * S3 仓库规格：目标桶与 key 前缀。
 *
 * @property bucket 存储桶名称（Rustfs / S3 兼容服务上的桶）。
 * @property prefix key 前缀（可选），所有对象 key 均以 `prefix/` 开头。
 */
data class S3RepositorySpec(
    val bucket: String,
    val prefix: String = "",
)

/**
 * [S3ArtifactManager] 的仓库解析配置。
 *
 * @property repositories 仓库名 → [S3RepositorySpec] 映射（上传/下载/晋升/删除均按仓库名解析）。
 * @property defaultRepository 未在 [repositories] 中命中的仓库名回退到的规格；为 null 时抛出配置错误。
 */
data class S3ArtifactManagerConfig(
    val repositories: Map<String, S3RepositorySpec> = emptyMap(),
    val defaultRepository: S3RepositorySpec? = null,
)

/**
 * [createS3Client] 的构建参数。
 *
 * @property endpointUrl S3 兼容服务地址（如 Rustfs / MinIO 的 `http://127.0.0.1:9000`）；为 null 时走 AWS 默认 endpoint。
 * @property region 签名区域；对自定义 endpoint 仅影响 SigV4 签名串，通常任意值均可。
 * @property accessKeyId 访问密钥；与 [secretAccessKey] 同时为空时走 AWS 默认凭证链（环境变量 / 配置文件 / IMDS）。
 * @property secretAccessKey 访问密钥。
 * @property forcePathStyle 使用路径风格寻址（`http://host/bucket/key`）。S3 兼容服务几乎都要求 true。
 * @property maxAttempts 每个请求的最大尝试次数（含首次）；为 null 时使用 SDK 默认（3 次）。
 * @property connectTimeoutSeconds 连接超时秒数，默认 10s
 * @property readTimeoutSeconds 读取超时秒数，默认 300s（大文件下载需更长）
 * @property writeTimeoutSeconds 写入超时秒数，默认 300s（大文件上传需更长）
 * @property enableAwsChunked 是否启用 aws-chunked 分块编码，默认 false。
 * 自建 S3 兼容存储（MinIO / Rustfs）不支持该编码，即使服务端能解析也会因签名算法
 * 不同而报 SignatureDoesNotMatch。
 * @property forceHttp11 强制使用 HTTP/1.1，默认 true。经 nginx/openresty 网关的
 * S3 兼容存储对大文件 PUT 走 HTTP/2 会返回 PROTOCOL_ERROR 并重置连接（上传超时），
 * 强制 HTTP/1.1 可规避。
 */
data class S3ClientOptions(
    val endpointUrl: String? = null,
    val region: String = "us-east-1",
    val accessKeyId: String? = null,
    val secretAccessKey: String? = null,
    val forcePathStyle: Boolean = true,
    val maxAttempts: Int? = null,
    val connectTimeoutSeconds: Int = 10,
    val readTimeoutSeconds: Int = 300,
    val writeTimeoutSeconds: Int = 300,
    val enableAwsChunked: Boolean = false,
    val forceHttp11: Boolean = true,
)

/**
 * 构建 [S3Client] 的工厂函数。
 *
 * 特点：
 * - 未显式提供凭证时使用 AWS 默认凭证链（环境变量、`~/.aws/credentials`、EC2 元数据等），
 *   与「支持从配置文件读取」的要求兼容，无需在代码中硬编码密钥；
 * - 显式注入 OkHttp HTTP 引擎（aws-sdk-kotlin 的引擎不在服务客户端构件中，必须自行提供）；
 * - 未配置 [S3ClientOptions.maxAttempts] 时沿用 SDK 内置重试策略（默认 3 次尝试）。
 *
 * ```
 * val s3 = createS3Client(
 *     S3ClientOptions(
 *         endpointUrl = "http://127.0.0.1:9000",
 *         accessKeyId = "minioadmin",
 *         secretAccessKey = "minioadmin",
 *     )
 * )
 * ```
 */
fun createS3Client(options: S3ClientOptions = S3ClientOptions()): S3Client = S3Client {
    region = options.region
    options.endpointUrl?.let { endpointUrl = Url.parse(it) }
    if (options.forcePathStyle) forcePathStyle = true
    options.maxAttempts?.let { attempts -> retryStrategy { maxAttempts = attempts } }
    // 默认关闭 aws-chunked：自建 S3 兼容存储不支持该签名分块编码，
    // 实测即使走 HTTP/1.1 也会报 SignatureDoesNotMatch。
    enableAwsChunked = options.enableAwsChunked
    val ak = options.accessKeyId?.takeIf { it.isNotBlank() }
    if (ak != null) {
        credentialsProvider = StaticCredentialsProvider {
            accessKeyId = ak
            secretAccessKey = options.secretAccessKey.orEmpty()
        }
    }
    // aws-sdk-kotlin 不会自动发现 HTTP 引擎，必须显式装配；OkHttp 引擎支持连接池与并发。
    // 三个超时都要透传：只设 connectTimeout 时读写超时会落到引擎默认值（30s），
    // 大文件上传/下载会被过早掐断。
    httpClient = OkHttpEngine {
        connectTimeout = options.connectTimeoutSeconds.seconds
        socketReadTimeout = options.readTimeoutSeconds.seconds
        socketWriteTimeout = options.writeTimeoutSeconds.seconds
        // 强制 HTTP/1.1：nginx/openresty 网关对大文件 PUT 走 HTTP/2 会
        // PROTOCOL_ERROR 重置连接（实测 60s 后 StreamResetException）。
        if (options.forceHttp11) {
            tlsContext { alpn = listOf(AlpnId.HTTP1_1) }
        }
    }
}

/**
 * 基于 Amazon S3（以及完全兼容 S3 API 的 Rustfs / MinIO 等对象存储）的 [ArtifactManager] 实现。
 *
 * ## 存储路径（Maven 风格）
 * ```
 * s3://<bucket>/<prefix>/<repository>/<groupPath>/<artifactId>/<version>/<artifactId>-<version>[-<classifier>].<packaging>
 * s3://my-bucket/repo/releases/com/example/app/1.0.0/app-1.0.0.jar
 * ```
 * 其中 `<repository>` 是仓库名（如 `releases` / `snapshots`），[S3RepositorySpec.prefix] 为可选全局前缀。
 *
 * ## 设计决策
 * - **非阻塞**：所有操作直接调用 aws-sdk-kotlin 的 suspend API（协程化异步引擎），不阻塞调用线程；
 * - **并发安全**：[S3Client] 线程安全，本类除不可变配置与日志外无状态，可被多条流水线并发使用；
 * - **版本控制**：上传时捕获 `versionId` 存入 [ArtifactReference.versionId]；下载/删除可精确到指定版本；
 * - **流式传输**：上传使用 `ByteStream.fromFile`，下载使用 `writeToFile` 落盘，大文件不载入内存；
 * - **幂等删除**：S3 的 `deleteObject` 对不存在的对象返回成功，删除天然幂等；
 * - **错误映射**：S3 服务异常统一转换为 [DevOpsError]（认证 / 未找到 / 网络 / 通用），供编排器决策。
 *
 * @property client 已配置好 endpoint / 凭证 / 引擎的 [S3Client]（见 [createS3Client]）。
 * @property config 仓库名 → 桶 / 前缀 的解析配置。
 */
open class S3ArtifactManager(
    private val client: S3Client,
    private val config: S3ArtifactManagerConfig = S3ArtifactManagerConfig(),
    private val logger: Logger = LoggerFactory.getLogger(S3ArtifactManager::class.java),
) : ArtifactManager {

    override suspend fun upload(
        ctx: PipelineContext,
        artifact: Artifact,
        repository: ArtifactRepository,
    ): ArtifactReference = ctx.trace("s3.upload") {
        val coordinates = resolveCoordinates(artifact)
        val repo = resolveRepository(repository.name)
        val key = objectKey(repo, repository.name, coordinates)
        val file = artifact.file.toFile()
        require(Files.isRegularFile(artifact.file)) {
            "artifact file does not exist or is not a regular file: ${artifact.file}"
        }

        logger.info(
            "s3 upload start: artifact={} -> s3://{}/{} (repository={})",
            artifact.name, repo.bucket, key, repository.name,
        )
        try {
            val response = client.putObject(
                PutObjectRequest {
                    bucket = repo.bucket
                    this.key = key
                    body = ByteStream.fromFile(file)
                    contentType = contentTypeFor(coordinates.packaging)
                    metadata = storageMetadata(artifact, coordinates, repository.name)
                }
            )
            val reference = ArtifactReference(
                coordinates = coordinates,
                repository = repository.name,
                storageKey = key,
                metadata = storageMetadata(artifact, coordinates, repository.name),
                bucket = repo.bucket,
                versionId = response.versionId,
            )
            logger.info(
                "s3 upload done: s3://{}/{} versionId={}",
                repo.bucket, key, response.versionId ?: "-",
            )
            reference
        } catch (e: Exception) {
            throw mapError("upload", e, mapOf("bucket" to repo.bucket, "key" to key))
        }
    }

    override suspend fun download(
        ctx: PipelineContext,
        reference: ArtifactReference,
        targetDirectory: Path,
    ): Artifact = ctx.trace("s3.download") {
        val repo = resolveRepository(reference.repository)
        val bucket = reference.bucket ?: repo.bucket
        val key = reference.storageKey
        Files.createDirectories(targetDirectory)
        val fileName = key.substringAfterLast('/').ifBlank { reference.coordinates.artifact }
        val target = targetDirectory.resolve(fileName)

        logger.info(
            "s3 download start: s3://{}/{} versionId={} -> {}",
            bucket, key, reference.versionId ?: "-", target,
        )
        try {
            // getObject 是流式操作：响应体只在 transform 块内可读，必须在块内消费（writeToFile 落盘），
            // 避免大文件载入内存；其余响应元数据通过块返回值带出。
            val downloaded = client.getObject(
                GetObjectRequest {
                    this.bucket = bucket
                    this.key = key
                    versionId = reference.versionId
                }
            ) { response ->
                // 流式写入本地文件；响应体理论上始终存在，若缺失直接报错而非空指针。
                response.body?.writeToFile(target.toFile())
                    ?: error("s3 download: empty response body for $bucket/$key")
                Downloaded(
                    sizeBytes = response.contentLength ?: Files.size(target),
                    metadata = response.metadata.orEmpty(),
                    versionId = response.versionId,
                )
            }
            val checksum = sha256(target)
            logger.info("s3 download done: {} ({} bytes)", target, downloaded.sizeBytes)
            Artifact(
                name = fileName,
                type = parseArtifactType(downloaded.metadata[KEY_ARTIFACT_TYPE]),
                file = target,
                checksum = checksum,
                sizeBytes = downloaded.sizeBytes,
                buildMetadata = downloaded.metadata + mapOf(
                    "bucket" to bucket,
                    "key" to key,
                    "versionId" to (downloaded.versionId ?: reference.versionId ?: ""),
                ),
            )
        } catch (e: Exception) {
            throw mapError("download", e, mapOf("bucket" to bucket, "key" to key))
        }
    }

    override suspend fun listVersions(
        ctx: PipelineContext,
        coordinates: Coordinates,
        repository: String,
    ): List<String> = ctx.trace("s3.list-versions") {
        val repo = resolveRepository(repository)
        val key = objectKey(repo, repository, coordinates)
        logger.info("s3 list-versions start: s3://{}/{} (coordinates={})", repo.bucket, key, coordinates.coordinateKey)
        try {
            val response = client.listObjectVersions(
                ListObjectVersionsRequest {
                    bucket = repo.bucket
                    prefix = key
                }
            )
            val versions = response.versions.orEmpty()
                .filter { it.key == key } // prefix 可能命中 key 的更长后缀，需精确匹配
                .sortedWith(compareByDescending { it.lastModified })
                .mapNotNull { it.versionId }
            // 未开启版本控制时 S3 不返回版本记录，此时返回空列表；调用方应视为「仅当前版本」。
            logger.info("s3 list-versions done: {} versions for s3://{}/{}", versions.size, repo.bucket, key)
            versions
        } catch (e: Exception) {
            throw mapError("list-versions", e, mapOf("bucket" to repo.bucket, "key" to key))
        }
    }

    override suspend fun promote(
        ctx: PipelineContext,
        reference: ArtifactReference,
        targetRepository: String,
    ): ArtifactReference = ctx.trace("s3.promote") {
        val srcRepo = resolveRepository(reference.repository)
        val srcBucket = reference.bucket ?: srcRepo.bucket
        val srcKey = reference.storageKey
        val dstRepo = resolveRepository(targetRepository)
        val dstKey = objectKey(dstRepo, targetRepository, reference.coordinates)

        logger.info(
            "s3 promote start: s3://{}/{} -> s3://{}/{}",
            srcBucket, srcKey, dstRepo.bucket, dstKey,
        )
        try {
            val response = client.copyObject(
                CopyObjectRequest {
                    bucket = dstRepo.bucket
                    key = dstKey
                    copySource = "$srcBucket/${encodeKeyForCopySource(srcKey)}"
                    // 默认 COPY：保留源对象的 x-amz-meta-* 元数据；晋升是复制，不删除源。
                    metadataDirective = MetadataDirective.Copy
                }
            )
            val promoted = ArtifactReference(
                coordinates = reference.coordinates,
                repository = targetRepository,
                storageKey = dstKey,
                metadata = reference.metadata,
                bucket = dstRepo.bucket,
                versionId = response.versionId,
            )
            logger.info("s3 promote done: -> s3://{}/{} versionId={}", dstRepo.bucket, dstKey, response.versionId ?: "-")
            promoted
        } catch (e: Exception) {
            throw mapError(
                "promote",
                e,
                mapOf("source" to "$srcBucket/$srcKey", "target" to "${dstRepo.bucket}/$dstKey"),
            )
        }
    }

    override suspend fun delete(ctx: PipelineContext, reference: ArtifactReference): Unit = ctx.trace("s3.delete") {
        val repo = resolveRepository(reference.repository)
        val bucket = reference.bucket ?: repo.bucket
        val key = reference.storageKey
        logger.info(
            "s3 delete start: s3://{}/{} versionId={}",
            bucket, key, reference.versionId ?: "-",
        )
        try {
            client.deleteObject(
                DeleteObjectRequest {
                    this.bucket = bucket
                    this.key = key
                    versionId = reference.versionId
                }
            )
            // S3 deleteObject 对不存在对象同样返回成功（204），删除幂等。
            logger.info("s3 delete done: s3://{}/{}", bucket, key)
        } catch (e: Exception) {
            throw mapError("delete", e, mapOf("bucket" to bucket, "key" to key))
        }
    }

    // ==================== 内部实现 ====================

    /** [getObject] 流式 transform 块返回值：下载文件的元数据快照。 */
    private data class Downloaded(
        val sizeBytes: Long,
        val metadata: Map<String, String>,
        val versionId: String?,
    )

    /** 解析仓库名 → [S3RepositorySpec]；未知仓库且无默认规格时抛配置错误。 */
    private fun resolveRepository(name: String): S3RepositorySpec =
        config.repositories[name] ?: config.defaultRepository
        ?: throw DevOpsError.fatal(
            ErrorCode.CONFIGURATION_INVALID,
            Stage.ARTIFACT_UPLOAD,
            "unknown artifact repository: $name (configure S3ArtifactManagerConfig.repositories)",
        )

    /** 依据坐标生成 Maven 风格对象 key：`<prefix>/<repository>/<groupPath>/<artifact>/<version>/<file>`。 */
    private fun objectKey(spec: S3RepositorySpec, repository: String, c: Coordinates): String =
        listOf(
            spec.prefix,
            repository,
            c.group.replace('.', '/'),
            c.artifact,
            c.version,
            fileName(c),
        ).filter { it.isNotBlank() }.joinToString("/")

    /** 对象文件名：`<artifactId>-<version>[-<classifier>].<packaging>`。 */
    private fun fileName(c: Coordinates): String = buildString {
        append(c.artifact).append('-').append(c.version)
        c.classifier?.takeIf { it.isNotBlank() }?.let { append('-').append(it) }
        append('.').append(c.packaging.ifBlank { "jar" })
    }

    /** 优先取 [Artifact.coordinates]，否则从 [Artifact.buildMetadata] 解析坐标。 */
    private fun resolveCoordinates(artifact: Artifact): Coordinates =
        artifact.coordinates ?: parseCoordinatesFromMetadata(artifact.buildMetadata)
        ?: throw DevOpsError.fatal(
            ErrorCode.CONFIGURATION_INVALID,
            Stage.ARTIFACT_UPLOAD,
            "cannot resolve coordinates for artifact ${artifact.name}: " +
                "set Artifact.coordinates or buildMetadata[groupId/artifactId/version]",
        )

    /**
     * 从元数据解析坐标。
     *
     * 同时接受两种键：S3 落盘后的小写形式（`groupid` 等，见 [storageMetadata]）与
     * 调用方习惯的驼峰形式（`groupId` 等），兼容不同来源的 [Artifact.buildMetadata]。
     */
    private fun parseCoordinatesFromMetadata(metadata: Map<String, String>): Coordinates? {
        fun value(vararg keys: String): String? = keys.firstNotNullOfOrNull { metadata[it] }
        val group = value(KEY_GROUP_ID, "groupId") ?: return null
        val artifact = value(KEY_ARTIFACT_ID, "artifactId") ?: return null
        val version = value(KEY_VERSION, "version") ?: return null
        return Coordinates(
            group = group,
            artifact = artifact,
            version = version,
            packaging = value(KEY_PACKAGING, "packaging") ?: "jar",
            classifier = value(KEY_CLASSIFIER, "classifier")?.takeIf { it.isNotBlank() },
        )
    }

    /** 上传时写入对象的用户元数据（S3 以 `x-amz-meta-*` 存储，键会被小写化）。 */
    private fun storageMetadata(artifact: Artifact, c: Coordinates, repository: String): Map<String, String> =
        mapOf(
            KEY_GROUP_ID to c.group,
            KEY_ARTIFACT_ID to c.artifact,
            KEY_VERSION to c.version,
            KEY_PACKAGING to c.packaging,
            KEY_CLASSIFIER to (c.classifier ?: ""),
            KEY_ARTIFACT_NAME to artifact.name,
            KEY_ARTIFACT_TYPE to artifact.type.name,
            KEY_REPOSITORY to repository,
        )

    private fun parseArtifactType(raw: String?): ArtifactType =
        raw?.let { runCatching { ArtifactType.valueOf(it) }.getOrNull() } ?: ArtifactType.GENERIC

    private fun contentTypeFor(packaging: String): String = when (packaging.lowercase()) {
        "jar", "war" -> "application/java-archive"
        "zip" -> "application/zip"
        "tar.gz", "tgz" -> "application/gzip"
        "json" -> "application/json"
        "xml" -> "application/xml"
        else -> "application/octet-stream"
    }

    /**
     * 计算文件 SHA-256，返回 `sha256:<hex>`（与 [cn.hubbo.utils.devops.impl.AbstractBuilder.calculateSha256] 一致）。
     */
    private fun sha256(path: Path): String {
        val digest = MessageDigest.getInstance("SHA-256")
        Files.newInputStream(path).use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) digest.update(buffer, 0, read)
        }
        return "sha256:" + digest.digest().joinToString("") { "%02x".format(it) }
    }

    /**
     * `copySource` 要求 key 以 URL 编码形式传递，但 `/` 是路径分隔符不能被编码。
     * 按段编码，并将 `+`（URLEncoder 对空格的产物）还原为 `%20`。
     */
    private fun encodeKeyForCopySource(key: String): String =
        key.split('/').joinToString("/") { segment ->
            URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20")
        }

    /** 将 S3 异常统一转换为 [DevOpsError]（认证 / 未找到 / 网络 / 通用）。 */
    private fun mapError(operation: String, e: Exception, context: Map<String, String>): DevOpsError {
        if (e is DevOpsError) return e
        val s3Error = e as? S3Exception
        val code = s3Error?.sdkErrorMetadata?.errorCode
        logger.warn("s3 {} failed (errorCode={}): {}", operation, code ?: "-", e.message, e)
        return when (code) {
            "NoSuchKey" -> DevOpsError.fatal(
                ErrorCode.ARTIFACT_NOT_FOUND, Stage.ARTIFACT_UPLOAD,
                "s3 $operation: object not found: ${context["bucket"]}/${context["key"]}", e, context,
            )

            "NoSuchBucket" -> DevOpsError.fatal(
                ErrorCode.CONFIGURATION_INVALID, Stage.ARTIFACT_UPLOAD,
                "s3 $operation: bucket does not exist: ${context["bucket"]}", e, context,
            )

            "AccessDenied", "InvalidAccessKeyId", "SignatureDoesNotMatch",
            "AuthorizationHeaderMalformed", "ExpiredToken", -> DevOpsError.fatal(
                ErrorCode.AUTH_FAILED, Stage.ARTIFACT_UPLOAD,
                "s3 $operation: authentication/authorization failed (${code}): ${e.message}", e, context,
            )

            "SlowDown", "RequestTimeout", "InternalError", "ServiceUnavailable" -> DevOpsError.recoverable(
                ErrorCode.NETWORK_ERROR, Stage.ARTIFACT_UPLOAD,
                "s3 $operation: transient failure (${code}): ${e.message}", e, context,
            )

            else -> DevOpsError.recoverable(
                ErrorCode.ARTIFACT_UPLOAD_FAILED, Stage.ARTIFACT_UPLOAD,
                "s3 $operation failed: ${e.message}", e, context,
            )
        }
    }

    companion object {
        // x-amz-meta-* 键（小写，S3 返回时保留小写）
        private const val KEY_GROUP_ID = "groupid"
        private const val KEY_ARTIFACT_ID = "artifactid"
        private const val KEY_VERSION = "version"
        private const val KEY_PACKAGING = "packaging"
        private const val KEY_CLASSIFIER = "classifier"
        private const val KEY_ARTIFACT_NAME = "artifact-name"
        private const val KEY_ARTIFACT_TYPE = "artifact-type"
        private const val KEY_REPOSITORY = "repository"

        /** 便捷工厂：组装客户端 + 默认配置。 */
        @JvmStatic
        fun create(
            client: S3Client,
            config: S3ArtifactManagerConfig = S3ArtifactManagerConfig(),
        ): S3ArtifactManager = S3ArtifactManager(client, config)
    }
}
