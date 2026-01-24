package cn.hubbo

import cn.hubbo.config.db.DbProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import java.util.*


@SpringBootApplication
@EnableConfigurationProperties(value = [DbProperties::class])
class SofaWebApplication {


    companion object {

        @JvmStatic
        private val logger: Logger = LoggerFactory.getLogger(SofaWebApplication::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            MDC.put("traceId", UUID.randomUUID().toString())
            val start = System.currentTimeMillis()
            val applicationContext = SpringApplication.run(SofaWebApplication::class.java, *args)
            val end = System.currentTimeMillis()
            logger.info("程序启动完成,耗时:{}ms", end - start)
            println(applicationContext)
        }

    }


}
