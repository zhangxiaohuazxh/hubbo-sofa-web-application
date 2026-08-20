package cn.hubbo.filter

import cn.hubbo.common.constants.LibraryConstants.*
import cn.hubbo.common.utils.TraceUtils
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class TraceIdWebFilter : WebFilter {

    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain
    ): Mono<Void> {
        // 优先沿用调用方传入的合法 traceId，否则生成新 traceId 并写入 MDC
        val requestTraceId = exchange.request.headers.getFirst(TRACE_ID_HEADER.value)
        val traceId = if (requestTraceId != null && TraceUtils.validTraceId(requestTraceId)) {
            requestTraceId
        } else {
            TraceUtils.getCurrentTraceId()
        }
        // 回写响应头，便于端到端排查
        exchange.response.headers.set(TRACE_ID_HEADER.value, traceId)
        return chain.filter(exchange)
            .contextWrite {
                it.put(COROUTINES_CONTEXT_KEY.value, mapOf(TRACE_ID.value to traceId))
            }

    }


}