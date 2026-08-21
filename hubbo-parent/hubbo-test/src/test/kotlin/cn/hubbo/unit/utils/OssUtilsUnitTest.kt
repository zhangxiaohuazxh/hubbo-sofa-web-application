package cn.hubbo.unit.utils

import cn.hubbo.utils.oss.OssUtils
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

object OssUtilsUnitTest {

    private val logger: Logger by lazy { LoggerFactory.getLogger(OssUtilsUnitTest::class.java) }

    @Disabled
    @Test
    fun testFileUpload(): Unit = runBlocking {
        val client = OssUtils.createClient(
            endpoint = "https://fs.hubbo.cn",
            regionName = "shanghai",
            accessKeyId = "",
            secretAccessKey = "",
            writeTimeoutSeconds = 600,
            readTimeoutSeconds = 600,
            connectTimeoutSeconds = 300
        )
        val file = File("/Users/yunjiang/Downloads/Proxyman_6.15.0.dmg")
        val res = OssUtils.uploadFile(
            client,
            "artifacts",
            file = file,
            key = "Proxyman_6.15.0.dmg",
            metadata = mapOf(
                "os" to "macos",
                "rawFilename" to file.name
            )
        )
        logger.info("res {}", res)
    }


}