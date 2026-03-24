package cn.hubbo.config.context

import cn.hubbo.common.constants.LibraryConstants.COROUTINES_CONTEXT_KEY
import io.micrometer.context.ContextRegistry
import io.micrometer.context.ThreadLocalAccessor
import org.slf4j.MDC
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import reactor.core.publisher.Hooks

@Component
class ContextPropagationConfig {

    @EventListener(ApplicationReadyEvent::class)
    fun init() {
        Hooks.enableAutomaticContextPropagation()
        ContextRegistry.getInstance().registerThreadLocalAccessor(object : ThreadLocalAccessor<Map<String, String>> {
            override fun key(): String = COROUTINES_CONTEXT_KEY.value
            override fun getValue(): Map<String, String> = MDC.getCopyOfContextMap() ?: emptyMap()
            override fun setValue(value: Map<String, String>) = MDC.setContextMap(value)
            override fun setValue() = MDC.clear()
        })
    }


}