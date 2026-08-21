package cn.hubbo.utils.oss

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.*
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.content.fromFile
import aws.smithy.kotlin.runtime.content.writeToFile
import aws.smithy.kotlin.runtime.http.engine.AlpnId
import aws.smithy.kotlin.runtime.http.engine.okhttp.OkHttpEngine
import aws.smithy.kotlin.runtime.net.url.Url
import kotlinx.coroutines.runBlocking
import org.apache.tika.Tika
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.io.File
import java.net.URLEncoder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.time.Duration.Companion.seconds
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider as JavaStaticCredentialsProvider
import software.amazon.awssdk.services.s3.model.GetObjectRequest as JavaGetObjectRequest
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest as JavaGetObjectPresignRequest

/**
 * Amazon S3 / MinIO / Rustfs 通用工具类
 *
 * 支持同步/异步操作，适用于简单的对象存储场景。
 * 复杂制品管理（版本控制、晋升、Maven 路径）请使用 [S3ArtifactManager]。
 * 分片上传（大文件）请使用 [MultipartUpload]。
 */
object OssUtils {

    /**
     * 创建 S3Client
     *
     * @param enableAwsChunked 是否启用 aws-chunked 分块编码（默认 false）。
     * 自建 S3 兼容存储（MinIO / Rustfs）不支持该编码，即使服务端能解析也会因签名算法
     * 不同而报 SignatureDoesNotMatch。
     * @param forceHttp11 强制使用 HTTP/1.1（默认 true）。经 nginx/openresty 网关的
     * S3 兼容存储对大文件 PUT 走 HTTP/2 会返回 PROTOCOL_ERROR 并重置连接（上传超时），
     * 强制 HTTP/1.1 可规避。
     */
    fun createClient(
        endpoint: String? = null,
        regionName: String = "us-east-1",
        accessKeyId: String? = null,
        secretAccessKey: String? = null,
        enableForcePathStyle: Boolean = true,
        maxAttempts: Int = 0,
        connectTimeoutSeconds: Int = 10,
        readTimeoutSeconds: Int = 300,
        writeTimeoutSeconds: Int = 300,
        enableAwsChunked: Boolean = false,
        forceHttp11: Boolean = true,
    ): S3Client = S3Client {
        region = regionName
        endpoint?.let { endpointUrl = Url.parse(it) }
        if (enableForcePathStyle) forcePathStyle = true
        // 默认关闭 aws-chunked：自建 S3 兼容存储不支持该签名分块编码，
        // 实测即使走 HTTP/1.1 也会报 SignatureDoesNotMatch。
        this.enableAwsChunked = enableAwsChunked
        retryStrategy { this.maxAttempts = maxAttempts }
        val ak = accessKeyId?.takeIf { it.isNotBlank() }
        if (ak != null) {
            credentialsProvider = StaticCredentialsProvider {
                this.accessKeyId = ak
                this.secretAccessKey = secretAccessKey.orEmpty()
            }
        }
        httpClient = OkHttpEngine {
            connectTimeout = connectTimeoutSeconds.seconds
            socketReadTimeout = readTimeoutSeconds.seconds
            socketWriteTimeout = writeTimeoutSeconds.seconds
            // 强制 HTTP/1.1：nginx/openresty 网关对大文件 PUT 走 HTTP/2 会
            // PROTOCOL_ERROR 重置连接（实测 60s 后 StreamResetException）。
            if (forceHttp11) {
                tlsContext { alpn = listOf(AlpnId.HTTP1_1) }
            }
        }
    }

    // ==================== 请求构建器（同步 API 使用） ====================

    private fun buildPutRequest(
        bucket: String,
        key: String,
        body: ByteStream,
        contentType: String,
        metadata: Map<String, String>,
    ): PutObjectRequest = PutObjectRequest {
        this.bucket = bucket
        this.key = key
        this.body = body
        this.contentType = contentType
        this.metadata = metadata
    }

    private fun buildGetRequest(
        bucket: String,
        key: String,
        versionId: String?,
    ): GetObjectRequest = GetObjectRequest {
        this.bucket = bucket
        this.key = key
        this.versionId = versionId
    }

    private fun buildDeleteRequest(
        bucket: String,
        key: String,
        versionId: String?,
    ): DeleteObjectRequest = DeleteObjectRequest {
        this.bucket = bucket
        this.key = key
        this.versionId = versionId
    }

    private fun buildHeadRequest(
        bucket: String,
        key: String,
        versionId: String?,
    ): HeadObjectRequest = HeadObjectRequest {
        this.bucket = bucket
        this.key = key
        this.versionId = versionId
    }

    private fun buildListRequest(
        bucket: String,
        prefix: String,
        delimiter: String?,
        maxKeys: Int,
        continuationToken: String?,
    ): ListObjectsV2Request = ListObjectsV2Request {
        this.bucket = bucket
        this.prefix = prefix
        this.delimiter = delimiter
        this.maxKeys = maxKeys
        this.continuationToken = continuationToken
    }

    private fun buildCopyRequest(
        destBucket: String,
        destKey: String,
        copySource: String,
        metadataDirective: MetadataDirective,
    ): CopyObjectRequest = CopyObjectRequest {
        this.bucket = destBucket
        this.key = destKey
        this.copySource = copySource
        this.metadataDirective = metadataDirective
    }

    // ==================== 同步 API (阻塞) ====================

    /**
     * 上传文件
     */
    fun uploadFile(
        client: S3Client,
        bucket: String,
        key: String,
        file: File,
        contentType: String? = null,
        metadata: Map<String, String> = emptyMap(),
    ): PutObjectResponse = runBlocking {
        val finalMetadata = metadata.takeIf { "rawFilename" in it } ?: metadata + ("rawFilename" to file.name)
        val request = buildPutRequest(
            bucket = bucket,
            key = key,
            body = ByteStream.fromFile(file),
            contentType = contentType ?: guessContentType(file),
            metadata = finalMetadata,
        )
        client.putObject(request)
    }

    /**
     * 上传字节数组
     */
    fun uploadBytes(
        client: S3Client,
        bucket: String,
        key: String,
        bytes: ByteArray,
        contentType: String = "application/octet-stream",
        metadata: Map<String, String> = emptyMap(),
    ): PutObjectResponse = runBlocking {
        val request = buildPutRequest(
            bucket = bucket,
            key = key,
            body = ByteStream.fromBytes(bytes),
            contentType = contentType,
            metadata = metadata,
        )
        client.putObject(request)
    }

    /**
     * 上传字符串
     */
    fun uploadString(
        client: S3Client,
        bucket: String,
        key: String,
        content: String,
        contentType: String = "text/plain; charset=utf-8",
        metadata: Map<String, String> = emptyMap(),
    ): PutObjectResponse =
        uploadBytes(client, bucket, key, content.toByteArray(StandardCharsets.UTF_8), contentType, metadata)

    /**
     * 下载文件到本地
     */
    fun downloadFile(
        client: S3Client,
        bucket: String,
        key: String,
        targetFile: File,
        versionId: String? = null,
    ): GetObjectResponse = runBlocking {
        val request = buildGetRequest(bucket, key, versionId)
        client.getObject(request) { response ->
            response.body?.writeToFile(targetFile.toPath())
            response
        }
    }

    /**
     * 下载为字节数组（通过临时文件实现）
     */
    fun downloadBytes(
        client: S3Client,
        bucket: String,
        key: String,
        versionId: String? = null,
    ): ByteArray = runBlocking {
        val request = buildGetRequest(bucket, key, versionId)
        val tempFile = File.createTempFile("s3-download-", ".tmp")
        try {
            client.getObject(request) { response ->
                response.body?.writeToFile(tempFile.toPath())
                response
            }
            Files.readAllBytes(tempFile.toPath())
        } finally {
            tempFile.delete()
        }
    }

    /**
     * 下载为字符串
     */
    fun downloadString(
        client: S3Client,
        bucket: String,
        key: String,
        charset: Charset = StandardCharsets.UTF_8,
        versionId: String? = null,
    ): String = downloadBytes(client, bucket, key, versionId).toString(charset)

    /**
     * 删除对象
     */
    fun deleteObject(
        client: S3Client,
        bucket: String,
        key: String,
        versionId: String? = null,
    ): DeleteObjectResponse = runBlocking {
        val request = buildDeleteRequest(bucket, key, versionId)
        client.deleteObject(request)
    }

    /**
     * 批量删除
     */
    fun deleteObjects(
        client: S3Client,
        bucket: String,
        keys: List<String>,
    ): DeleteObjectsResponse = runBlocking {
        val request = DeleteObjectsRequest {
            this.bucket = bucket
            delete = Delete {
                objects = keys.map { ObjectIdentifier { key = it } }
                quiet = true
            }
        }
        client.deleteObjects(request)
    }

    /**
     * 检查对象是否存在
     */
    fun objectExists(
        client: S3Client,
        bucket: String,
        key: String,
    ): Boolean = runBlocking {
        val request = buildHeadRequest(bucket, key, null)
        try {
            client.headObject(request)
            true
        } catch (e: S3Exception) {
            if (e.sdkErrorMetadata?.errorCode == "NotFound" || e.sdkErrorMetadata?.errorCode == "NoSuchKey") false
            else throw e
        }
    }

    /**
     * 获取对象元数据
     */
    fun getObjectMetadata(
        client: S3Client,
        bucket: String,
        key: String,
        versionId: String? = null,
    ): HeadObjectResponse = runBlocking {
        val request = buildHeadRequest(bucket, key, versionId)
        client.headObject(request)
    }

    /**
     * 列出对象（支持分页）
     */
    fun listObjects(
        client: S3Client,
        bucket: String,
        prefix: String = "",
        delimiter: String? = null,
        maxKeys: Int = 1000,
        continuationToken: String? = null,
    ): ListObjectsV2Response = runBlocking {
        val request = buildListRequest(bucket, prefix, delimiter, maxKeys, continuationToken)
        client.listObjectsV2(request)
    }

    /**
     * 列出所有对象（自动分页）
     */
    fun listAllObjects(
        client: S3Client,
        bucket: String,
        prefix: String = "",
    ): Sequence<Object> = sequence {
        var token: String? = null
        do {
            val response = listObjects(client, bucket, prefix, maxKeys = 1000, continuationToken = token)
            response.contents?.forEach { yield(it) }
            token = response.nextContinuationToken
        } while (token != null && token.isNotBlank())
    }

    /**
     * 复制对象（同桶或跨桶）
     */
    fun copyObject(
        client: S3Client,
        sourceBucket: String,
        sourceKey: String,
        destBucket: String,
        destKey: String,
        sourceVersionId: String? = null,
        metadataDirective: MetadataDirective = MetadataDirective.Copy,
    ): CopyObjectResponse = runBlocking {
        val copySource = "$sourceBucket/${encodeKeyForCopySource(sourceKey)}"
        val request = buildCopyRequest(destBucket, destKey, copySource, metadataDirective)
        client.copyObject(request)
    }

    /**
     * 生成预签名 URL（临时访问链接）
     *
     * 使用 Java SDK v2 的 S3Presigner 生成带 AWS Signature V4 签名的 URL。
     *
     * @param contentDisposition "download" 默认下载行为，"preview" 浏览器内预览（图片/视频等）
     */
    fun generatePresignedUrl(
        client: S3Client,
        bucket: String,
        key: String,
        expirationSeconds: Int = 3600,
        contentDisposition: FileOperationEnum = FileOperationEnum.DOWNLOAD,
    ): String = runBlocking {
        val creds = client.config.credentialsProvider?.resolve()
            ?: error("No credentials found in client")
        val region = client.config.region?.let { Region.of(it) } ?: Region.US_EAST_1
        val endpoint = client.config.endpointUrl?.toString()
            ?: "https://s3.${client.config.region}.amazonaws.com"

        val presignerBuilder = S3Presigner.builder()
            .region(region)
            .credentialsProvider(
                JavaStaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        creds.accessKeyId,
                        creds.secretAccessKey
                    )
                )
            )
            .endpointOverride(java.net.URI.create(endpoint))
            .serviceConfiguration(
                software.amazon.awssdk.services.s3.S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .build()
            )

        presignerBuilder.build().use { presigner ->
            val getObjectRequestBuilder = JavaGetObjectRequest.builder()
                .bucket(bucket)
                .key(key)

            when (contentDisposition) {
                FileOperationEnum.PREVIEW -> {
                    getObjectRequestBuilder.responseContentDisposition("inline")
                    // 根据文件扩展名推断 Content-Type，确保浏览器能正确预览
                    val filename = key.substringAfterLast('/')
                    getObjectRequestBuilder.responseContentType(guessContentType(filename))
                }
                FileOperationEnum.DOWNLOAD -> {
                    val filename = key.substringAfterLast('/')
                    getObjectRequestBuilder.responseContentDisposition("attachment; filename=\"$filename\"")
                }
            }

            val presignRequest = JavaGetObjectPresignRequest.builder()
                .signatureDuration(java.time.Duration.ofSeconds(expirationSeconds.toLong()))
                .getObjectRequest(getObjectRequestBuilder.build())
                .build()

            presigner.presignGetObject(presignRequest).url().toString()
        }
    }

    // ==================== 异步 API (协程) ====================

    /**
     * 异步上传文件
     */
    suspend fun uploadFileAsync(
        client: S3Client,
        bucket: String,
        key: String,
        file: File,
        contentType: String? = null,
        metadata: Map<String, String> = emptyMap(),
    ): PutObjectResponse {
        val finalMetadata = metadata.takeIf { "rawFilename" in it } ?: metadata + ("rawFilename" to file.name)
        val request = buildPutRequest(
            bucket = bucket,
            key = key,
            body = ByteStream.fromFile(file),
            contentType = contentType ?: guessContentType(file),
            metadata = finalMetadata,
        )
        return client.putObject(request)
    }

    /**
     * 异步下载文件
     */
    suspend fun downloadFileAsync(
        client: S3Client,
        bucket: String,
        key: String,
        targetFile: File,
        versionId: String? = null,
    ): GetObjectResponse {
        val request = buildGetRequest(bucket, key, versionId)
        return client.getObject(request) { response ->
            response.body?.writeToFile(targetFile.toPath())
            response
        }
    }

    /**
     * 异步删除对象
     */
    suspend fun deleteObjectAsync(
        client: S3Client,
        bucket: String,
        key: String,
        versionId: String? = null,
    ): DeleteObjectResponse {
        val request = buildDeleteRequest(bucket, key, versionId)
        return client.deleteObject(request)
    }

    // ==================== 工具方法 ====================

    private val tika = Tika()

    /**
     * 基于文件内容检测 Content-Type（使用 Apache Tika 魔数检测）
     */
    fun guessContentType(file: File): String = tika.detect(file)

    /**
     * 仅根据文件名扩展名猜测 Content-Type（降级方案，无文件内容时使用）
     */
    fun guessContentType(fileName: String): String = when {
        fileName.endsWith(".jar") || fileName.endsWith(".war") -> "application/java-archive"
        fileName.endsWith(".zip") -> "application/zip"
        fileName.endsWith(".tar.gz") || fileName.endsWith(".tgz") -> "application/gzip"
        fileName.endsWith(".json") -> "application/json"
        fileName.endsWith(".xml") -> "application/xml"
        fileName.endsWith(".yaml") || fileName.endsWith(".yml") -> "application/yaml"
        fileName.endsWith(".txt") -> "text/plain"
        fileName.endsWith(".html") || fileName.endsWith(".htm") -> "text/html"
        fileName.endsWith(".css") -> "text/css"
        fileName.endsWith(".js") -> "application/javascript"
        fileName.endsWith(".png") -> "image/png"
        fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") -> "image/jpeg"
        fileName.endsWith(".gif") -> "image/gif"
        fileName.endsWith(".pdf") -> "application/pdf"
        else -> "application/octet-stream"
    }

    /**
     * 计算文件 SHA-256
     */
    fun sha256(file: File): String = sha256(file.toPath())

    fun sha256(path: Path): String {
        val digest = MessageDigest.getInstance("SHA-256")
        Files.newInputStream(path).use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        return "sha256:" + digest.digest().joinToString("") { "%02x".format(it) }
    }

    /**
     * 计算字节数组 SHA-256
     */
    fun sha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(bytes)
        return "sha256:" + digest.digest().joinToString("") { "%02x".format(it) }
    }

    /**
     * copySource key 编码（保留 / 不编码）
     */
    fun encodeKeyForCopySource(key: String): String =
        key.split('/').joinToString("/") { segment ->
            URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20")
        }

    /**
     * 构建 Maven 风格对象 key
     * `<prefix>/<repository>/<groupPath>/<artifact>/<version>/<artifact>-<version>[-<classifier>].<packaging>`
     */
    fun buildMavenKey(
        prefix: String,
        repository: String,
        groupId: String,
        artifactId: String,
        version: String,
        packaging: String = "jar",
        classifier: String? = null,
    ): String = listOf(
        prefix,
        repository,
        groupId.replace('.', '/'),
        artifactId,
        version,
        buildFileName(artifactId, version, packaging, classifier),
    ).filter { it.isNotBlank() }.joinToString("/")

    private fun buildFileName(
        artifactId: String,
        version: String,
        packaging: String,
        classifier: String?,
    ): String = buildString {
        append(artifactId).append('-').append(version)
        classifier?.takeIf { it.isNotBlank() }?.let { append('-').append(it) }
        append('.').append(packaging)
    }

    // ==================== 通用分页辅助 ====================

    /**
     * 分页遍历所有对象
     */
    fun <T> paginate(
        fetchPage: (String?) -> Pair<List<T>, String?>,
        onPage: (List<T>) -> Unit,
    ) {
        var token: String? = null
        do {
            val (items, nextToken) = fetchPage(token)
            onPage(items)
            token = nextToken
        } while (token != null && token.isNotBlank())
    }
}

enum class FileOperationEnum {
    DOWNLOAD,
    PREVIEW
}
