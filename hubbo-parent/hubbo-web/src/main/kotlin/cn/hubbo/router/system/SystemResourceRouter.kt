package cn.hubbo.router.system

import cn.hubbo.handler.system.SystemResourceHandler
import cn.hubbo.handler.test.TestHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter

@Configuration
class SystemResourceRouter {

    @Bean
    fun tetsRoute(handler: TestHandler): RouterFunction<ServerResponse> {
        return coRouter {
            "/test".nest {
                GET("/datetime", handler::systemTime)
                GET("/push", handler::pushMessage)
            }
        }
    }

    @Bean
    fun menuRoute(handler: SystemResourceHandler): RouterFunction<ServerResponse> {
        return coRouter {
            "/sys".nest {
                "/menu".nest {
                    GET("/list", handler::queryAllMenus)
                }
                "/server".nest {
                    GET("/time", handler::requestServerTime)
                    GET("/millils", handler::requestServerTimeMillis)
                }
            }
        }
    }


}