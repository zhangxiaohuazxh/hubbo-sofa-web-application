package cn.hubbo.utils.devops.impl

import cn.hubbo.utils.CommandLineUtils
import cn.hubbo.utils.devops.DevOps
import cn.hubbo.utils.devops.LocalStorageInfo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class RustDevOpsImpl : DevOps {

    private val logger: Logger = LoggerFactory.getLogger(RustDevOpsImpl::class.java)

    override fun getLogger(): Logger {
        return logger
    }

    override suspend fun compileCommand(): String {
        return "cargo c"
    }

    override suspend fun check(localStorageInfo: LocalStorageInfo) {
        CommandLineUtils.exec("cargo check", workingDirectory = localStorageInfo.path)
    }

    override suspend fun build(localStorageInfo: LocalStorageInfo) {
        CommandLineUtils.exec("cargo build --release", workingDirectory = localStorageInfo.path)
    }

    override suspend fun test(localStorageInfo: LocalStorageInfo) {
        CommandLineUtils.exec("cargo test", workingDirectory = localStorageInfo.path)
    }

    override fun isFinalProduct(file: File): Boolean {
        TODO("Not yet implemented")
    }

}