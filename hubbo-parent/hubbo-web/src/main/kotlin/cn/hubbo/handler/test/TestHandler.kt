package cn.hubbo.handler.test

import cn.hubbo.service.facade.sys.SysCommonService
import cn.hubbo.utils.extension.currentTimeString
import jakarta.annotation.Resource
import org.apache.rocketmq.client.core.RocketMQClientTemplate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.messaging.support.GenericMessage
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import java.time.Duration
import java.time.LocalDateTime

@Controller
class TestHandler(val sysCommonService: SysCommonService) {


    private val logger: Logger by lazy { LoggerFactory.getLogger(TestHandler::class.java) }

    //    @Resource
    //    private lateinit var rocketMQTemplate: RocketMQClientTemplate


    suspend fun systemTime(request: ServerRequest): ServerResponse {
        logger.info("访问系统时间接口")
        //        val messageViews = rocketMQTemplate.receive(3, Duration.ofMinutes(1))
        //        logger.info("接收到的消息 {}", messageViews)
        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValueAndAwait(mapOf("data" to LocalDateTime.now().currentTimeString()))
    }

    suspend fun pushMessage(request: ServerRequest): ServerResponse {
        logger.info("推送消息")
        //        rocketMQTemplate.syncSendNormalMessage("hetu", "first message")
        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValueAndAwait(mapOf("data" to "success"))
    }


}