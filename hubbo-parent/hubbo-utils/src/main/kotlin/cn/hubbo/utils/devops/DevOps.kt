package cn.hubbo.utils.devops

import cn.hubbo.utils.CommandLineUtils
import cn.hubbo.utils.DevOpsUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.FalseFileFilter
import org.apache.commons.io.filefilter.FileFileFilter
import org.apache.commons.io.filefilter.IOFileFilter
import org.apache.commons.io.filefilter.TrueFileFilter
import org.slf4j.Logger
import java.io.File

interface DevOps {


    // 默认实现 直接删除满足特征的文件夹
    suspend fun clean() {
        getLogger().info("清理旧的编译产物")
    }


    fun getDevOpsConfiguration(): DevOpsConfiguration


    // 默认实现
    suspend fun clone() {
        val configuration = getDevOpsConfiguration()
        val result =
            CommandLineUtils.execute(
                "rm -rf ${configuration.projectDirectory()} && git clone ${configuration.url}"
            )
        val logger = getLogger()
        logger.info("执行结果 {} {}", result.exitCode, result.output)
    }


    // 语法检查
    suspend fun check() {

    }


    // 编译
    suspend fun compileCommand(): String


    suspend fun compile() {
        val logger = getLogger()
        logger.info("==========================开始编译==========================")
        clean()
        CommandLineUtils.execute(compileCommand(), workingDirectory = getDevOpsConfiguration().projectDirectory())
        logger.info("==========================编译完成==========================")
    }


    // 构建
    suspend fun build()


    // 测试
    suspend fun test() {

    }


    fun getLogger(): Logger


    // 判断文件是否是最终编译的产物
    fun isFinalProduct(file: File): Boolean


    // 捕获构建的最终产物
    suspend fun captureProduct(): List<File> {
        val files = FileUtils.listFiles(
            getDevOpsConfiguration().projectDirectory(),
            FileFileFilter.INSTANCE,
            TrueFileFilter.INSTANCE
        )
        return files.filter { isFinalProduct(it) }
    }


}


data class DevOpsConfiguration(
    val url: String,
    val basePath: File = FileUtils.getTempDirectory(),
    val projectName: String = DevOpsUtils.parseRepositoryName(url)
) {

    fun projectDirectory(): File {
        return File("$basePath${File.separator}$projectName")
    }

}
