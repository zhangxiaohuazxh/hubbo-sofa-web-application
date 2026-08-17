package cn.hubbo.utils.devops.impl

import cn.hubbo.utils.CommandLineUtils
import cn.hubbo.utils.FileUtils
import cn.hubbo.utils.devops.DevOps
import cn.hubbo.utils.devops.LocalStorageInfo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class JavaDevOpsImpl : DevOps {

    private val logger: Logger = LoggerFactory.getLogger(JavaDevOpsImpl::class.java)

    override suspend fun check(localStorageInfo: LocalStorageInfo) {
        CommandLineUtils.exec("mvn validate", workingDirectory = localStorageInfo.path)
    }

    override suspend fun build(localStorageInfo: LocalStorageInfo) {
        CommandLineUtils.exec("mvn build -B -e", workingDirectory = localStorageInfo.path)
    }

    override suspend fun test(localStorageInfo: LocalStorageInfo) {
        CommandLineUtils.exec("mvn test", workingDirectory = localStorageInfo.path)
    }

    override fun getLogger(): Logger {
        return logger
    }

    override suspend fun compileCommand(): String {
        return "mvn clean compile package"
    }

    override fun isFinalProduct(file: File): Boolean {
        return file.isFile && file.name.endsWith(".jar") && FileUtils.isExecutableJar(file)
    }

}