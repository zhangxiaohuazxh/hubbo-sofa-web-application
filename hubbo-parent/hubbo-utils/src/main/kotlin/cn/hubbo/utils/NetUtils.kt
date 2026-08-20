package cn.hubbo.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.NetworkInterface.getNetworkInterfaces
import java.net.Socket

class NetUtils {


    companion object {

        private val logger: Logger by lazy { LoggerFactory.getLogger(NetUtils::class.java) }

        private val virtualNetWorkInterfaceNames =
            listOf<String>("VIRTUAL", "TUNNEL", "TAP", "VNIC", "VMNET", "VBOX", "PPP")

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

        @JvmStatic
        fun isVirtualNetWorkInterface(`interface`: NetworkInterface): Boolean {
            val displayName = `interface`.displayName
            //val hardwareAddress = `interface`.hardwareAddress
            return virtualNetWorkInterfaceNames.any {
                displayName.uppercase().contains(it)
            }
        }

        @JvmStatic
        fun getLocalHosts(log: Logger = logger): List<NetworkInterface> {
            val list = ArrayList<NetworkInterface>(10)
            runCatching {
                getNetworkInterfaces().iterator().forEach { it ->
                    if (!it.isLoopback && !it.isVirtual && it.isUp && !isVirtualNetWorkInterface(it)) {
                        list.add(it)
                    }
                }
            }.onFailure {
                log.error("获取本机ip失败", it)
            }
            return list
        }

        @JvmStatic
        fun getLocalHost(log: Logger = logger): String {
            val hosts: List<NetworkInterface> = getLocalHosts()
            if (!hosts.isEmpty()) {
                for (`interface` in hosts) {
                    val displayName = `interface`.displayName.uppercase()
                    if (displayName.contains("INTEL") || displayName.contains("REALTEK")) {
                        val iterator = `interface`.inetAddresses.iterator()
                        while (iterator.hasNext()) {
                            val address = iterator.next()
                            if (address is Inet4Address) {
                                return address.hostAddress
                            }
                        }
                    }
                }
            }
            // 未匹配到物理网卡时回退到回环地址，避免返回空串导致日志模板里 ip 为空
            return InetAddress.getLoopbackAddress().hostAddress
        }


    }


}