package cn.hubbo.unit.utils

import cn.hubbo.apt.annotation.processor.ForeignFunctionInterfaceBindingProcessor
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import javax.tools.JavaFileObject
import javax.tools.SimpleJavaFileObject
import javax.tools.StandardLocation
import javax.tools.ToolProvider

class JavaCodeParserUnitTest {

    @Disabled
    @Test
    fun testSimpleParseDemo() {
        // 创建编译器实例
        val compiler = ToolProvider.getSystemJavaCompiler()
        val fileManager = compiler.getStandardFileManager(null, null, null)
        // 创建临时目录用于输出
        val tempDir = File("D:\\tmp\\cache\\apt")
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, mutableListOf<File>(tempDir))
        val sourceCode =
            FileUtils.readFileToString(File(tempDir, "cn\\hubbo\\FFIUsage.java"), StandardCharsets.UTF_8)

        val simpleClassName = "FFIUsage.java"
        val targetFile = File(tempDir, "cn\\hubbo\\FFIUsageImpl.java")
        if (targetFile.exists()) {
            targetFile.delete()
        }
        // 创建 JavaFileObject
        val fileObject = object : SimpleJavaFileObject(
            Paths.get(simpleClassName).toUri(),
            JavaFileObject.Kind.SOURCE
        ) {
            override fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence {
                return sourceCode
            }
        }
        val processor = ForeignFunctionInterfaceBindingProcessor()
        // 执行编译和注解处理
        val task = compiler.getTask(
            null,
            fileManager,
            null,
            listOf(
                "-proc:only",
                "-processor",
                "cn.hubbo.apt.annotation.processor.ForeignFunctionInterfaceBindingProcessor"
            ),
            null,
            listOf(fileObject)
        )
        // 设置处理器
        task.setProcessors(listOf(processor))
        // 执行
        val success = task.call()
        // 验证结果
        assert(success) { "Compilation failed" }
    }


}
