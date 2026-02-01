package cn.hubbo.unit.io

import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Paths
import kotlin.io.path.exists

class FileAPIUnitTest {

    private val logger: Logger by lazy { LoggerFactory.getLogger(FileAPIUnitTest::class.java) }

    @Test
    fun testPathAPI() {
        val userHome = System.getProperty("user.home")
        val paths = Paths.get(userHome, ".app", ".conf")
        if (paths.exists()) {
            logger.info("该目录存在{}", paths.toAbsolutePath())
        } else {
            logger.warn("目录不存在")
        }
    }

}