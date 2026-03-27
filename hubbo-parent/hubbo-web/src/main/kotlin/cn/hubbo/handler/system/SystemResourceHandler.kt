package cn.hubbo.handler.system

import cn.hubbo.entity.vo.ResultVO.Companion.success
import cn.hubbo.utils.extension.currentTimeString
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.bodyValueWithTypeAndAwait
import java.lang.System.currentTimeMillis
import java.time.LocalDateTime.now

@Controller
class SystemResourceHandler {

    suspend fun requestServerTime(request: ServerRequest): ServerResponse {
        val _ = request
        return ServerResponse.ok().bodyValueWithTypeAndAwait(success(now().currentTimeString()))
    }

    suspend fun requestServerTimeMillis(request: ServerRequest): ServerResponse {
        val _ = request
        return ServerResponse.ok().bodyValueWithTypeAndAwait(success(currentTimeMillis()))
    }


    suspend fun queryAllMenus(request: ServerRequest): ServerResponse {
        val _ = request
        return ServerResponse.ok().bodyValueAndAwait(success<Any>())
    }


}