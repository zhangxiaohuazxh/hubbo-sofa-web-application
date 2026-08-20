package cn.hubbo.unit.devops

import cn.hubbo.utils.devops.core.PipelineContexts
import cn.hubbo.utils.devops.core.error.DevOpsError
import cn.hubbo.utils.devops.core.error.ErrorCode
import cn.hubbo.utils.devops.core.model.Artifact
import cn.hubbo.utils.devops.core.model.ArtifactReference
import cn.hubbo.utils.devops.core.model.ArtifactRepository
import cn.hubbo.utils.devops.core.model.ArtifactType
import cn.hubbo.utils.devops.core.model.Coordinates
import cn.hubbo.utils.devops.core.model.RepositoryType
import cn.hubbo.utils.devops.impl.S3ArtifactManager
import cn.hubbo.utils.devops.impl.S3ArtifactManagerConfig
import cn.hubbo.utils.devops.impl.S3ClientOptions
import cn.hubbo.utils.devops.impl.S3RepositorySpec
import cn.hubbo.utils.devops.impl.createS3Client
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.CopyObjectResponse
import aws.sdk.kotlin.services.s3.model.GetObjectResponse
import aws.sdk.kotlin.services.s3.model.MetadataDirective
import aws.sdk.kotlin.services.s3.model.ObjectVersion
import aws.sdk.kotlin.services.s3.model.PutObjectResponse
import aws.sdk.kotlin.services.s3.model.S3Exception
import aws.smithy.kotlin.runtime.content.ByteStream
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

/**
 * [S3ArtifactManager] 单元测试（mockk 模拟 [S3Client]，无需真实 S3 / Rustfs 服务）。
 *
 * 集成测试思路（需要 Docker 或本地 Rustfs/MinIO 时）：
 * - 用 testcontainers-localstack 或 S3Mock 起一个 S3 兼容服务，
 *   `createS3Client(S3ClientOptions(endpointUrl = "http://127.0.0.1:9000", accessKeyId = "...", secretAccessKey = "..."))`
 *   指向该服务，随后对同一仓库执行 upload → listVersions → download → promote → delete 的端到端断言。
 */
class S3ArtifactManagerTest {

    @TempDir
    lateinit var tempDir: Path

    private val client: S3Client = mockk()

    private val config = S3ArtifactManagerConfig(
        repositories = mapOf(
            "releases" to S3RepositorySpec(bucket = "artifacts", prefix = "repo"),
            "snapshots" to S3RepositorySpec(bucket = "artifacts-snap"),
        ),
    )

    private val manager = S3ArtifactManager(client, config)

    private val ctx = PipelineContexts.default()

    private val coordinates = Coordinates(group = "com.example", artifact = "app", version = "1.0.0", packaging = "jar")

    private fun artifactFile(content: String = "hello s3"): Path =
        tempDir.resolve("app-1.0.0.jar").apply { Files.writeString(this, content) }

    private fun artifact(content: String = "hello s3"): Artifact {
        val file = artifactFile(content)
        return Artifact(
            name = "app-1.0.0.jar",
            type = ArtifactType.JAR,
            file = file,
            checksum = "sha256:x",
            sizeBytes = Files.size(file),
            coordinates = coordinates,
        )
    }

    /** 构造 smithy-kotlin 的 [aws.smithy.kotlin.runtime.time.Instant]。 */
    private fun s3Time(value: String): aws.smithy.kotlin.runtime.time.Instant =
        aws.smithy.kotlin.runtime.time.Instant(java.time.Instant.parse(value))

    // ==================== upload ====================

    @Test
    fun `upload 构建 Maven 风格 key 并捕获 versionId`() = runTest {
        coEvery { client.putObject(any()) } returns PutObjectResponse { versionId = "v1" }

        val reference = manager.upload(
            ctx,
            artifact(),
            ArtifactRepository(name = "releases", type = RepositoryType.S3, url = "s3://artifacts"),
        )

        assertEquals("repo/releases/com/example/app/1.0.0/app-1.0.0.jar", reference.storageKey)
        assertEquals("artifacts", reference.bucket)
        assertEquals("v1", reference.versionId)
        assertEquals(coordinates, reference.coordinates)
        coVerify {
            client.putObject(
                match {
                    it.bucket == "artifacts" &&
                        it.key == "repo/releases/com/example/app/1.0.0/app-1.0.0.jar" &&
                        it.metadata?.get("artifactid") == "app" &&
                        it.metadata?.get("groupid") == "com.example"
                }
            )
        }
    }

    @Test
    fun `upload 支持 classifier 与 buildMetadata 坐标回退`() = runTest {
        coEvery { client.putObject(any()) } returns PutObjectResponse { versionId = "v1" }
        val withClassifier = coordinates.copy(classifier = "sources")

        val reference = manager.upload(
            ctx,
            artifact().copy(
                coordinates = null,
                buildMetadata = mapOf(
                    "groupId" to withClassifier.group,
                    "artifactId" to withClassifier.artifact,
                    "version" to withClassifier.version,
                    "classifier" to "sources",
                ),
            ),
            ArtifactRepository(name = "releases", type = RepositoryType.S3, url = "s3://artifacts"),
        )

        assertEquals("repo/releases/com/example/app/1.0.0/app-1.0.0-sources.jar", reference.storageKey)
    }

    @Test
    fun `upload 缺少坐标时抛出配置错误`() = runTest {
        val e = org.junit.jupiter.api.assertThrows<DevOpsError> {
            manager.upload(
                ctx,
                artifact().copy(coordinates = null),
                ArtifactRepository(name = "releases", type = RepositoryType.S3, url = "s3://artifacts"),
            )
        }
        assertEquals(ErrorCode.CONFIGURATION_INVALID, e.code)
    }

    @Test
    fun `upload 未知仓库时抛出配置错误`() = runTest {
        val e = org.junit.jupiter.api.assertThrows<DevOpsError> {
            manager.upload(
                ctx,
                artifact(),
                ArtifactRepository(name = "nightly", type = RepositoryType.S3, url = "s3://artifacts"),
            )
        }
        assertEquals(ErrorCode.CONFIGURATION_INVALID, e.code)
    }

    // ==================== download ====================

    @Test
    fun `download 流式落盘并返回 Artifact`() = runTest {
        coEvery { client.getObject<Any>(any(), any()) } coAnswers {
            val block = secondArg<suspend (GetObjectResponse) -> Any>()
            block(
                GetObjectResponse {
                    body = ByteStream.fromBytes("hello s3".toByteArray())
                    contentLength = 8L
                    metadata = mapOf(
                        "artifact-type" to ArtifactType.JAR.name,
                        "groupid" to "com.example",
                    )
                    versionId = "v1"
                }
            )
        }

        val targetDir = tempDir.resolve("out")
        val downloaded = manager.download(
            ctx,
            ArtifactReference(
                coordinates = coordinates,
                repository = "releases",
                storageKey = "repo/releases/com/example/app/1.0.0/app-1.0.0.jar",
                bucket = "artifacts",
                versionId = "v1",
            ),
            targetDir,
        )

        assertEquals("app-1.0.0.jar", downloaded.name)
        assertEquals(ArtifactType.JAR, downloaded.type)
        assertEquals("hello s3", Files.readString(downloaded.file))
        assertEquals(8L, downloaded.sizeBytes)
        assertTrue(downloaded.checksum.startsWith("sha256:"))
        coVerify { client.getObject<Any>(match { it.bucket == "artifacts" && it.versionId == "v1" }, any()) }
    }

    @Test
    fun `download NoSuchKey 映射为 ARTIFACT_NOT_FOUND`() = runTest {
        coEvery { client.getObject<Any>(any(), any()) } throws s3Exception("NoSuchKey")

        val e = org.junit.jupiter.api.assertThrows<DevOpsError> {
            manager.download(
                ctx,
                ArtifactReference(coordinates, "releases", "repo/releases/com/example/app/1.0.0/app-1.0.0.jar"),
                tempDir,
            )
        }
        assertEquals(ErrorCode.ARTIFACT_NOT_FOUND, e.code)
    }

    // ==================== listVersions ====================

    @Test
    fun `listVersions 仅返回精确 key 的版本并按时间倒序`() = runTest {
        val key = "repo/releases/com/example/app/1.0.0/app-1.0.0.jar"
        coEvery { client.listObjectVersions(any()) } returns aws.sdk.kotlin.services.s3.model.ListObjectVersionsResponse {
            versions = listOf(
                ObjectVersion { this.key = "$key.bak"; versionId = "v-old"; lastModified = s3Time("2026-01-01T00:00:00Z") },
                ObjectVersion { this.key = key; versionId = "v1"; lastModified = s3Time("2026-03-01T00:00:00Z") },
                ObjectVersion { this.key = key; versionId = "v2"; lastModified = s3Time("2026-02-01T00:00:00Z") },
            )
        }

        val versions = manager.listVersions(ctx, coordinates, "releases")

        assertEquals(listOf("v1", "v2"), versions) // 精确 key 匹配 + 新→旧
        coVerify { client.listObjectVersions(match { it.prefix == key && it.bucket == "artifacts" }) }
    }

    @Test
    fun `listVersions 未开启版本控制时返回空列表`() = runTest {
        coEvery { client.listObjectVersions(any()) } returns aws.sdk.kotlin.services.s3.model.ListObjectVersionsResponse {
            versions = emptyList()
        }

        assertTrue(manager.listVersions(ctx, coordinates, "releases").isEmpty())
    }

    // ==================== promote ====================

    @Test
    fun `promote 复制到目标仓库并保留元数据，不删除源`() = runTest {
        coEvery { client.copyObject(any()) } returns CopyObjectResponse { versionId = "p1" }

        val promoted = manager.promote(
            ctx,
            ArtifactReference(
                coordinates = coordinates,
                repository = "releases",
                storageKey = "repo/releases/com/example/app/1.0.0/app-1.0.0.jar",
                bucket = "artifacts",
                versionId = "v1",
            ),
            "snapshots",
        )

        assertEquals("snapshots", promoted.repository)
        assertEquals("artifacts-snap", promoted.bucket)
        // key 结构：<prefix>/<repository>/<groupPath>/... ；snapshots 无 prefix，故以仓库名开头
        assertEquals("snapshots/com/example/app/1.0.0/app-1.0.0.jar", promoted.storageKey)
        assertEquals("p1", promoted.versionId)
        coVerify {
            client.copyObject(
                match {
                    it.bucket == "artifacts-snap" &&
                        it.copySource == "artifacts/repo/releases/com/example/app/1.0.0/app-1.0.0.jar" &&
                        it.metadataDirective == MetadataDirective.Copy
                }
            )
        }
        // 晋升是复制：源对象不得被删除
        coVerify(exactly = 0) { client.deleteObject(any()) }
    }

    @Test
    fun `promote 对特殊字符 key 做 copySource 编码`() = runTest {
        coEvery { client.copyObject(any()) } returns CopyObjectResponse { versionId = "p1" }

        manager.promote(
            ctx,
            ArtifactReference(
                coordinates = coordinates.copy(version = "1.0.0+build"),
                repository = "releases",
                storageKey = "repo/releases/com/example/app/1.0.0+build/app-1.0.0+build.jar",
                bucket = "artifacts",
            ),
            "snapshots",
        )

        coVerify {
            client.copyObject(
                match { it.copySource!!.contains("1.0.0%2Bbuild") } // '+' 被编码为 %2B，'/' 保留
            )
        }
    }

    // ==================== delete ====================

    @Test
    fun `delete 携带 versionId 精确删除`() = runTest {
        coEvery { client.deleteObject(any()) } returns aws.sdk.kotlin.services.s3.model.DeleteObjectResponse {}

        manager.delete(
            ctx,
            ArtifactReference(
                coordinates = coordinates,
                repository = "releases",
                storageKey = "repo/releases/com/example/app/1.0.0/app-1.0.0.jar",
                versionId = "v1",
            ),
        )

        coVerify { client.deleteObject(match { it.versionId == "v1" && it.bucket == "artifacts" }) }
    }

    // ==================== 错误映射 ====================

    @Test
    fun `AccessDenied 映射为 AUTH_FAILED`() = runTest {
        coEvery { client.putObject(any()) } throws s3Exception("AccessDenied")

        val e = org.junit.jupiter.api.assertThrows<DevOpsError> {
            manager.upload(ctx, artifact(), ArtifactRepository("releases", RepositoryType.S3, "s3://artifacts"))
        }
        assertEquals(ErrorCode.AUTH_FAILED, e.code)
    }

    @Test
    fun `临时性错误映射为 NETWORK_ERROR 可恢复`() = runTest {
        coEvery { client.putObject(any()) } throws s3Exception("ServiceUnavailable")

        val e = org.junit.jupiter.api.assertThrows<DevOpsError> {
            manager.upload(ctx, artifact(), ArtifactRepository("releases", RepositoryType.S3, "s3://artifacts"))
        }
        assertEquals(ErrorCode.NETWORK_ERROR, e.code)
        assertTrue(e.recoverability == cn.hubbo.utils.devops.core.error.Recoverability.RECOVERABLE)
    }

    // ==================== 客户端工厂 ====================

    @Test
    fun `createS3Client 指向自定义 endpoint（不发起网络请求）`() {
        val s3 = createS3Client(
            S3ClientOptions(
                endpointUrl = "http://127.0.0.1:9000",
                accessKeyId = "minioadmin",
                secretAccessKey = "minioadmin",
            )
        )
        assertEquals("http://127.0.0.1:9000", s3.config.endpointUrl.toString())
        assertTrue(s3.config.forcePathStyle)
        assertEquals("us-east-1", s3.config.region)
    }

    /** 构造携带指定 errorCode 的 [S3Exception]（mock 属性链，规避 smithy 内部 API）。 */
    private fun s3Exception(code: String): S3Exception = mockk {
        every { message } returns code
        every { sdkErrorMetadata.errorCode } returns code
    }
}
