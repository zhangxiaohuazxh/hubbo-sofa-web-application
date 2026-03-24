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
        val traceId = exchange.request.headers.getFirst(TRACE_ID_HEADER.value) ?: TraceUtils.getCurrentTraceId()
        return chain.filter(exchange)
            .contextWrite {
                it.put(COROUTINES_CONTEXT_KEY.value, mapOf(TRACE_ID.value to traceId))
            }

    }


}