package cn.hubbo.handler.test

import cn.hubbo.entity.vo.ResultVO.Companion.success
import cn.hubbo.utils.extension.currentTimeString
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import java.time.LocalDateTime

@Controller
class TestHandler {


    private val logger: Logger by lazy { LoggerFactory.getLogger(TestHandler::class.java) }


    suspend fun systemTime(request: ServerRequest): ServerResponse {
        logger.info("访问系统时间接口")
        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValueAndAwait(success(LocalDateTime.now().currentTimeString()))
    }

    suspend fun pushMessage(request: ServerRequest): ServerResponse {
        logger.info("推送消息")
        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValueAndAwait(success("success"))
    }


}