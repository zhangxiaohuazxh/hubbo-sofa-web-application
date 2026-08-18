package cn.hubbo.unit.devops

import cn.hubbo.utils.devops.DevOpsConfiguration
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
        val ops = JavaDevOpsImpl(DevOpsConfiguration("https://gitee.com/xuxueli0323/xxl-job.git"))
        val localStorageInfo = ops.clone()
        logger.info("项目存储信息 {}", localStorageInfo)
        ops.compile()
        ops.build()
    }

    @Test
    fun testCaptureFinalProduct(): Unit = runBlocking {
        val devOpsConfiguration = DevOpsConfiguration("https://gitee.com/xuxueli0323/xxl-job.git")
        val ops = JavaDevOpsImpl(devOpsConfiguration)
        val files = ops.captureProduct()
        for (file in files) {
            logger.info("构建的产物 {}", file)
        }
    }


}


object RustDevOpsUnitTest {


}