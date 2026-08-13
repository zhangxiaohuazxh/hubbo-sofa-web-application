package cn.hubbo.utils

import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.Executor
import org.apache.commons.exec.PumpStreamHandler
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.function.Consumer

object CommandLineUtils {


    @JvmStatic
    fun exec(
        command: String,
        consumer: Consumer<Executor> = {},
        charset: Charset = StandardCharsets.UTF_8
    ): CommandExecutedResult {
        var command = command
        if (System.getProperty("os.name").contains("Windows")) {
            command = "cmd /c $command"
        }
        //  每次创建一个executor实例保证线程安全
        val defaultExecutor = DefaultExecutor.builder().get()
        val outputStream = ByteArrayOutputStream()
        val errStream = ByteArrayOutputStream()
        val streamHandler = PumpStreamHandler(outputStream, errStream)
        defaultExecutor.streamHandler = streamHandler
        consumer.accept(defaultExecutor)
        val code = defaultExecutor.execute(CommandLine.parse(command))
        if (!defaultExecutor.isFailure(code)) {
            return CommandExecutedResult(0, outputStream.toString(charset))
        }
        return CommandExecutedResult(-1, errStream.toString(charset))
    }


}

data class CommandExecutedResult(val exitCode: Int, val output: String)