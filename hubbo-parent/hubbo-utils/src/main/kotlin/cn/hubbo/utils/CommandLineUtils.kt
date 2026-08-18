package cn.hubbo.utils

import dev.jbang.jash.Jash
import kotlinx.coroutines.Dispatchers
import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.ExecuteException
import org.apache.commons.exec.ExecuteWatchdog
import org.apache.commons.exec.Executor
import org.apache.commons.exec.PumpStreamHandler
import org.apache.commons.io.FileUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.function.Consumer
import dev.jbang.jash.Jash.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.OutputStream

object CommandLineUtils {

    private val logger: Logger by lazy { LoggerFactory.getLogger(CommandLineUtils::class.java) }

    /**
     * bash 执行指定的命令
     *
     * @param command 执行的命令
     * @param consumer 执行器的扩展操作
     * @param charset 字符集
     * @param timeoutMillis 任务执行的超时时间
     */
    @JvmStatic
    suspend fun exec(
        command: String,
        consumer: Consumer<Executor> = {},
        charset: Charset = StandardCharsets.UTF_8,
        timeoutMillis: Long = 30_000L,
        workingDirectory: File = FileUtils.getTempDirectory()
    ): CommandExecutedResult {
        // 统一通过 shell 执行,以支持管道、&&、cd 等 shell 语法。
        // 不能直接 CommandLine.parse(command) 按空白拆分参数: 它不解释 &&、|、cd 等语法,
        // 会把整串命令当作第一个程序(如 git)的普通参数,导致 "too many arguments" 之类的错误。
        val commandLine = if (System.getProperty("os.name").contains("Windows")) {
            CommandLine("cmd").addArgument("/c").addArgument(command, false)
        } else {
            CommandLine("bash").addArgument("-c").addArgument(command, false)
        }
        //  每次创建一个executor实例保证线程安全
        val defaultExecutor = DefaultExecutor.builder().get()
        val outputStream = ByteArrayOutputStream()
        val errStream = ByteArrayOutputStream()
        val streamHandler = PumpStreamHandler(outputStream, errStream)
        defaultExecutor.streamHandler = streamHandler
        defaultExecutor.workingDirectory = workingDirectory
        consumer.accept(defaultExecutor)
        // 超时保护: 某些命令(如 ping 不带 -c)会永不退出,超时后强制杀掉子进程,避免调用方永久卡死
        with(Dispatchers.IO) {
            val watchdog = ExecuteWatchdog.builder().setTimeout(Duration.ofMillis(timeoutMillis)).get()
            defaultExecutor.watchdog = watchdog
            return try {
                val code = defaultExecutor.execute(commandLine)
                when {
                    watchdog.killedProcess() -> CommandExecutedResult(
                        -2,
                        errStream.toString(charset).ifBlank { "命令执行超时(${timeoutMillis}ms),已强制终止: $command" })

                    !defaultExecutor.isFailure(code) -> CommandExecutedResult(0, outputStream.toString(charset))
                    else -> CommandExecutedResult(-1, errStream.toString(charset))
                }
            } catch (e: ExecuteException) {
                val timedOut = watchdog.killedProcess()
                CommandExecutedResult(
                    if (timedOut) -2 else -1, errStream.toString(charset).ifBlank { "命令执行失败: $command" })
            }
        }
    }

    suspend fun execute(
        command: String,
        outputStream: OutputStream = System.out,
        timeout: Duration = Duration.ofSeconds(30),
        workingDirectory: File = FileUtils.getTempDirectory()
    ): CommandExecutedResult {
        val res = runCatching {
            val directory = "cd ${workingDirectory.path}"
            val command = shell("$directory && $command").withAnyExitCode().withTimeout(timeout)
            command.streamBytes().use { stream ->
                stream.forEachOrdered { outputStream.write(it) }
                outputStream.flush()
            }
            if (command.isSuccessful) CommandExecutedResult(0, null)
            else CommandExecutedResult(-1, null)
        }
        return res.fold(
            onSuccess = { it },
            onFailure = {
                logger.info("任务执行出错", it)
                CommandExecutedResult(-1, it.message)
            }
        )
    }


}

data class CommandExecutedResult(val exitCode: Int, val output: String?)