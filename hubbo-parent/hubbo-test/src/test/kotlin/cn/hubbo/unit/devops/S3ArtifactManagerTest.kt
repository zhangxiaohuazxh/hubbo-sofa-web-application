package cn.hubbo.unit.devops

import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.asByteStream
import cn.hubbo.utils.devops.impl.S3ClientOptions
import cn.hubbo.utils.devops.impl.createS3Client
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class S3ArtifactManagerTest {

    private val logger: Logger by lazy { LoggerFactory.getLogger(S3ArtifactManagerTest::class.java) }

    @Disabled
    @Test
    fun testFileUpload(): Unit = runBlocking {
        val s3Client = createS3Client(
            S3ClientOptions(
                endpointUrl = "https://fs.hubbo.cn",
                region = "shanghai",
                accessKeyId = "",
                secretAccessKey = "",
                forcePathStyle = true
            )
        )
        val request = PutObjectRequest {
            bucket = "artifacts"
            key = "th.jpeg"
            body = File("/Users/yunjiang/Downloads/th.jpeg").asByteStream()
        }
        val response = s3Client.putObject(request)
        logger.info("response {}", response)
    }


}
