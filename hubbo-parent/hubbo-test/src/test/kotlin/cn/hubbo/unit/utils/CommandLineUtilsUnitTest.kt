package cn.hubbo.unit.utils

import cn.hubbo.utils.CommandExecutedResult
import cn.hubbo.utils.CommandLineUtils
import dev.jbang.jash.Jash
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.charset.Charset


object CommandLineUtilsUnitTest {

    private val logger: Logger by lazy { LoggerFactory.getLogger(CommandLineUtilsUnitTest::class.java) }

    @Test
    fun testExecSimpleCommand(): Unit = runBlocking {
        val result = CommandLineUtils.exec("git status")
        logger.info("任务执行结果 {}", result)
    }

    @Test
    fun testExecSimpleCommandAtSpecifiedDirectory(): Unit = runBlocking {
        var result: CommandExecutedResult
        if (!System.getProperty("os.name").contains("Win")) {
            result = CommandLineUtils.exec(
                "ping www.baidu.com",
                { it.workingDirectory = File(FileUtils.getTempDirectoryPath()) },
                timeoutMillis = 1000
            )
        } else {
            result = CommandLineUtils.exec(
                "ping www.hubbo.cn", { it.workingDirectory = File(FileUtils.getTempDirectoryPath()) },
                Charset.forName("GB18030"),
                timeoutMillis = 1000
            )
        }
        logger.info("执行结果 {}", result)
    }

    @Test
    fun testCloneRepository(): Unit = runBlocking {
        logger.info("默认字符集 {}", Charset.defaultCharset())
        logger.info("默认的临时目录 {}", FileUtils.getTempDirectoryPath())
        val res =
            CommandLineUtils.exec(
                "rm -rf xxl-job && git clone https://gitee.com/xuxueli0323/xxl-job.git && cd xxl-job && mvn clean compile package",
                timeoutMillis = 60_000L
            )
        if (res.exitCode != 0) {
            logger.info("任务执行失败 {}", res.output)
        }
    }

    @Test
    fun testJashExecute(): Unit = runBlocking {
        val res = CommandLineUtils.execute("pwd")
        logger.run { info("执行结果 {}", res) }
    }

}