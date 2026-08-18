package cn.hubbo.utils.devops.impl

import cn.hubbo.utils.CommandLineUtils
import cn.hubbo.utils.FileUtils
import cn.hubbo.utils.devops.DevOps
import cn.hubbo.utils.devops.DevOpsConfiguration
import cn.hubbo.utils.devops.LocalStorageInfo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class JavaDevOpsImpl : DevOps {

    private var devOpsConfiguration: DevOpsConfiguration? = null

    constructor(devOpsConfiguration: DevOpsConfiguration) {
        this.devOpsConfiguration = devOpsConfiguration
    }

    private val logger: Logger = LoggerFactory.getLogger(JavaDevOpsImpl::class.java)


    override suspend fun build() {
        CommandLineUtils.execute("mvn package -B -e", workingDirectory = getDevOpsConfiguration().projectDirectory())
    }

    override suspend fun check() {
        CommandLineUtils.execute("mvn validate", workingDirectory = getDevOpsConfiguration().projectDirectory())
    }

    override suspend fun test() {
        CommandLineUtils.execute("mvn test", workingDirectory = getDevOpsConfiguration().projectDirectory())
    }

    override fun getLogger(): Logger {
        return logger
    }

    override fun getDevOpsConfiguration(): DevOpsConfiguration {
        return devOpsConfiguration!!
    }

    override suspend fun compileCommand(): String {
        return "mvn clean compile package"
    }

    override fun isFinalProduct(file: File): Boolean {
        return file.isFile && file.name.endsWith(".jar") && FileUtils.isExecutableJar(file)
    }

}