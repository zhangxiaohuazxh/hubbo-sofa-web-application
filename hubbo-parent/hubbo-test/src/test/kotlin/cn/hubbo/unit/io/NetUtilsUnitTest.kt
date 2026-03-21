package cn.hubbo.unit.io

import cn.hubbo.utils.NetUtils
import cn.hubbo.utils.NetUtils.Companion.getLocalHosts
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class NetUtilsUnitTest {

    private val logger: Logger by lazy { LoggerFactory.getLogger(NetUtilsUnitTest::class.java) }

    @Test
    fun testGetLocalHosts() {
        val hosts = getLocalHosts()
        logger.info("获取到的本机host信息 {}", hosts)
    }

    @Test
    fun testGetLocalHost() {
        val localHost = NetUtils.getLocalHost()
        logger.info("获取到的本机local host信息 {}", localHost)
    }

}