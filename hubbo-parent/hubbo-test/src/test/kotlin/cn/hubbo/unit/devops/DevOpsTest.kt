package cn.hubbo.unit.devops

import cn.hubbo.utils.devops.LocalStorageInfo
import cn.hubbo.utils.devops.impl.JavaDevOpsImpl
import cn.hubbo.utils.devops.impl.RustDevOpsImpl
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

object JavaDevOpsUnitTest {

    private val logger: Logger by lazy { LoggerFactory.getLogger(JavaDevOpsUnitTest::class.java) }

    @Test
    fun testClone(): Unit = runBlocking {
        val ops = JavaDevOpsImpl()
        val localStorageInfo = ops.clone("https://gitee.com/xuxueli0323/xxl-job.git")
        logger.info("项目存储信息 {}", localStorageInfo)
        ops.compile(localStorageInfo)
        ops.build(localStorageInfo)
    }

    @Test
    fun testCaptureFinalProduct(): Unit = runBlocking {
        val ops = JavaDevOpsImpl()
        val files = ops.captureProduct(
            LocalStorageInfo(
                url = "https://gitee.com/xuxueli0323/xxl-job.git",
                path = File(FileUtils.getTempDirectory(), "xxl-job")
            )
        )
        for (file in files) {
            logger.info("构建的产物 {}", file)
        }
    }


}


object RustDevOpsUnitTest {


}