package cn.hubbo.lifecycle

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.web.server.reactive.context.ReactiveWebServerInitializedEvent
import org.springframework.context.ApplicationListener


class ApplicationInitializer : ApplicationListener<ReactiveWebServerInitializedEvent> {

    private val logger: Logger by lazy { LoggerFactory.getLogger(ApplicationInitializer::class.java) }

    override fun onApplicationEvent(event: ReactiveWebServerInitializedEvent) {
        System.setProperty("server.port", "$event.webServer.port")
        logger.info("重新加载log4j2配置信息")
        logger.info("log4j2配置重新加载成功")
    }
}
