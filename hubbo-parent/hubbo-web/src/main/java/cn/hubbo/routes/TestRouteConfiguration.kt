package cn.hubbo.routes

import cn.hubbo.handler.TestRouteHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter

@Configuration
class TestRouteConfiguration {


    @Bean
    fun tetsRoute(handler: TestRouteHandler): RouterFunction<ServerResponse> {
        return coRouter {
            "/test".nest {
                GET("/datetime", handler::systemTime)
            }
        }
    }


}