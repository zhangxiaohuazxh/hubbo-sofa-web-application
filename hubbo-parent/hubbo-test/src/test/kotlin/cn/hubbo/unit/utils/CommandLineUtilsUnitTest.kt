package cn.hubbo.unit.utils

import cn.hubbo.utils.CommandExecutedResult
import cn.hubbo.utils.CommandLineUtils
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.charset.Charset

object CommandLineUtilsUnitTest {

    private val logger: Logger by lazy { LoggerFactory.getLogger(CommandLineUtilsUnitTest::class.java) }

    @Test
    fun testExecSimpleCommand() {
        val result = CommandLineUtils.exec("git status")
        logger.info("执行结果 {}", result)
    }

    @Test
    fun testExecSimpleCommandAtSpecifiedDirectory() {
        var result: CommandExecutedResult
        if (!System.getProperty("os.name").contains("Win")) {
            result = CommandLineUtils.exec(
                "ping www.baidu.com",
                { it.workingDirectory = File(FileUtils.getTempDirectoryPath()) })
        } else {
            result = CommandLineUtils.exec(
                "ping www.hubbo.cn", { it.workingDirectory = File(FileUtils.getTempDirectoryPath()) },
                Charset.forName("GB18030")
            )
        }
        logger.info("执行结果 {}", result)
    }


}