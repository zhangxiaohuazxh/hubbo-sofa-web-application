package cn.hubbo.utils.devops.impl

import cn.hubbo.utils.CommandLineUtils
import cn.hubbo.utils.devops.DevOps
import cn.hubbo.utils.devops.DevOpsConfiguration
import cn.hubbo.utils.devops.LocalStorageInfo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class RustDevOpsImpl : DevOps {

    private val logger: Logger = LoggerFactory.getLogger(RustDevOpsImpl::class.java)

    private var devOpsConfiguration: DevOpsConfiguration? = null

    constructor(devOpsConfiguration: DevOpsConfiguration) {
        this.devOpsConfiguration = devOpsConfiguration
    }


    override fun getLogger(): Logger {
        return logger
    }

    override fun getDevOpsConfiguration(): DevOpsConfiguration {
        return devOpsConfiguration!!
    }

    override suspend fun compileCommand(): String {
        return "cargo c"
    }

    override suspend fun build() {
        CommandLineUtils.execute("cargo build", workingDirectory = getDevOpsConfiguration().projectDirectory())
    }

    override fun isFinalProduct(file: File): Boolean {
        TODO("Not yet implemented")
    }

}