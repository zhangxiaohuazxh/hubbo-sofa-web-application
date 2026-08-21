package cn.hubbo.unit.utils

import aws.sdk.kotlin.services.s3.S3Client
import cn.hubbo.utils.oss.FileOperationEnum
import cn.hubbo.utils.oss.OssUtils
import kotlinx.coroutines.runBlocking
import org.apache.tika.Tika
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.File

@Disabled
object OssUtilsUnitTest {

    private val logger: Logger by lazy { LoggerFactory.getLogger(OssUtilsUnitTest::class.java) }

    @JvmStatic
    fun s3Client(): S3Client = OssUtils.createClient(
        endpoint = "https://fs.hubbo.cn",
        regionName = "shanghai",
        accessKeyId = "",
        secretAccessKey = "",
        writeTimeoutSeconds = 600,
        readTimeoutSeconds = 600,
        connectTimeoutSeconds = 300
    )

    private val bucket: String = "artifacts"

    @Disabled
    @Test
    fun testFileUpload(): Unit = runBlocking {
        val file = File("/Users/yunjiang/Downloads/Proxyman_6.15.0.dmg")
        val res = OssUtils.uploadFile(
            s3Client(),
            bucket,
            file = file,
            key = "Proxyman_6.15.0.dmg",
            metadata = mapOf(
                "os" to "macos",
                "rawFilename" to file.name
            )
        )
        logger.info("res {}", res)
    }

    @Test
    fun testUploadBytes(): Unit = runBlocking {
        val buffer = ByteArrayInputStream("".toByteArray())
        val response = OssUtils.uploadBytes(
            s3Client(), bucket, "bytes.txt", "abc".toByteArray(), metadata = mapOf(
                "os" to "windows"
            )
        )
        logger.info("响应结果 {}", response)
    }

    @Test
    fun testTikaCheckFileType() {
        val file = File("c:\\Users\\33233\\Downloads\\1757592308937-4002.mp4")
        val type = Tika().detect(file)
        logger.info("type $type")
    }

    @Test
    fun testDownloadFile() {
        val result = OssUtils.downloadFile(
            s3Client(),
            bucket = bucket,
            key = "th.jpeg",
            targetFile = File("c:\\Users\\33233\\Downloads\\th.jpeg")
        )
        logger.info("download result {}", result)
    }


    @Test
    fun testDownloadBytes() {
        val res = OssUtils.downloadBytes(s3Client(), bucket = bucket, key = "bytes.txt")
        logger.info("download bytes res {}", String(res))
    }

    @Test
    fun testDeleteFile() {
        val response = OssUtils.deleteObject(s3Client(), bucket = bucket, key = "bytes.txt")
        logger.info("delete res {}", response)
    }

    @Test
    fun testCheckFileExists() {
        val bool = OssUtils.objectExists(s3Client(), bucket = bucket, key = "bytes.txt")
        logger.info("检查结果 {}", if (bool) "文件存在" else "文件不存在")
    }

    @Test
    fun testListObjects() {
        val objects = OssUtils.listObjects(s3Client(), bucket = bucket)
        for (obj in objects.contents!!) {
            logger.info("查询到的object {}", obj)
        }
    }

    @Test
    fun testGenerateShareUrl() {
        val url =
            OssUtils.generatePresignedUrl(
                s3Client(),
                bucket = bucket,
                key = "th.jpeg",
                contentDisposition = FileOperationEnum.PREVIEW,
                expirationSeconds = 10
            )
        logger.info("url {}", url)
    }


}