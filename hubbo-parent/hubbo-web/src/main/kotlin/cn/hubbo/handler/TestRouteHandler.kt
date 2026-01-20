package cn.hubbo.handler

import cn.hubbo.utils.extension.currentTimeString
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import java.time.LocalDateTime

@Component
class TestRouteHandler {


    private val logger: Logger by lazy {
        LoggerFactory.getLogger(TestRouteHandler::class.java)
    }

    suspend fun systemTime(request: ServerRequest): ServerResponse {
        logger.info("访问系统时间接口")
        return ServerResponse.ok()
            .contentType(APPLICATION_JSON)
            .bodyValueAndAwait(mapOf("data" to LocalDateTime.now().currentTimeString()))
    }


}