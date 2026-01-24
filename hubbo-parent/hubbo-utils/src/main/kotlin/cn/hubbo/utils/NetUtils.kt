package cn.hubbo.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.net.Socket

class NetUtils {


    companion object {

        private val logger: Logger by lazy { LoggerFactory.getLogger(NetUtils::class.java) }

        /**
         * 判断网络是否可达
         *
         * @param host 域名 不要带协议 示例 www.baidu.com
         * @param port 端口
         * @param timeout 超时时间 默认3s
         * @param log logger
         * @return 是否可达
         */
        @JvmStatic
        fun isReachable(host: String, port: Int, timeout: Int = 3000, log: Logger = logger): Boolean {
            return runCatching {
                Socket().use {
                    it.connect(InetSocketAddress(host, port), timeout)
                }
                true
            }.onFailure { exception ->
                log.error("host:{} port:{}网络不可达", host, port, exception)
            }.getOrDefault(false)
        }


    }


}