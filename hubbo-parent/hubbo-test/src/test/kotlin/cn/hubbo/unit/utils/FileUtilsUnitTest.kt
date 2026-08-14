package cn.hubbo.unit.utils

import cn.hubbo.utils.FileUtils
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

object FileUtilsUnitTest {

    private val logger: Logger by lazy { LoggerFactory.getLogger(FileUtilsUnitTest::class.java) }

    @Test
    fun testDirTree(): Unit = runBlocking {
        val tree =
            FileUtils.markProjectStructureTree(File("/var/folders/7j/h381gyln3tqg0932kh9p4r2m0000gn/T/xxl-job"), "java")
        logger.info("tree: \n{}", tree.name + "\n" + tree.render())
    }

}