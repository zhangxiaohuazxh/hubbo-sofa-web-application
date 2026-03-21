package cn.hubbo

import cn.hubbo.config.db.DbProperties
import cn.hubbo.utils.NetUtils
import com.google.common.base.Stopwatch.createStarted
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.server.reactive.context.ReactiveWebServerApplicationContext


@SpringBootApplication
@EnableConfigurationProperties(value = [DbProperties::class])
class SofaWebApplication {


    companion object {

        @JvmStatic
        private val logger: Logger = LoggerFactory.getLogger(SofaWebApplication::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            val stopwatch = createStarted()
            System.setProperty("ip.address", NetUtils.getLocalHost())
            val applicationContext = SpringApplication.run(SofaWebApplication::class.java, *args)
            val port = (applicationContext as ReactiveWebServerApplicationContext).webServer?.port.toString().orEmpty()
            System.setProperty("server.port", port)
            val loggerContext = LogManager.getContext(false) as LoggerContext
            loggerContext.reconfigure()
            loggerContext.updateLoggers()
            val millis = stopwatch.stop().elapsed().toMillis()
            logger.info("程序启动完成,耗时:{}ms", millis)
        }

    }


}
