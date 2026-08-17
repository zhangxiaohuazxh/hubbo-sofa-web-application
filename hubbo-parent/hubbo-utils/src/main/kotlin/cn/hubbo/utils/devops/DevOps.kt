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


    // 默认实现
    suspend fun clone(url: String, timeoutMillis: Long = 30_000L): LocalStorageInfo {
        val projectName = DevOpsUtils.parseRepositoryName(url)
        val result = CommandLineUtils.exec("rm -rf $projectName && git clone $url", timeoutMillis = timeoutMillis)
        val logger = getLogger()
        logger.info("执行结果 {} {}", result.exitCode, result.output)
        return LocalStorageInfo(url, File(FileUtils.getTempDirectory(), projectName))
    }


    // 语法检查
    suspend fun check(localStorageInfo: LocalStorageInfo)


    // 编译
    suspend fun compileCommand(): String

    suspend fun compile(localStorageInfo: LocalStorageInfo) {
        val logger = getLogger()
        logger.info("==========================开始编译==========================")
        clean()
        CommandLineUtils.exec(compileCommand(), workingDirectory = localStorageInfo.path)
        logger.info("==========================编译完成==========================")
    }

    // 构建
    suspend fun build(localStorageInfo: LocalStorageInfo)


    // 测试
    suspend fun test(localStorageInfo: LocalStorageInfo)


    fun workingDirectory(): File {
        return FileUtils.getTempDirectory()
    }

    fun getLogger(): Logger

    // 判断文件是否是最终编译的产物
    fun isFinalProduct(file: File): Boolean

    // 捕获构建的最终产物
    suspend fun captureProduct(localStorageInfo: LocalStorageInfo): List<File> {
        val files = FileUtils.listFiles(localStorageInfo.path, FileFileFilter.INSTANCE, TrueFileFilter.INSTANCE)
        return files.filter { isFinalProduct(it) }
    }


}