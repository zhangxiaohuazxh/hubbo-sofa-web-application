package cn.hubbo.router.test

import cn.hubbo.handler.test.TestHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter

@Configuration
class TestRouter {


    @Bean
    fun tetsRoute(handler: TestHandler): RouterFunction<ServerResponse> {
        return coRouter {
            "/test".nest {
                GET("/datetime", handler::systemTime)
            }
        }
    }


}