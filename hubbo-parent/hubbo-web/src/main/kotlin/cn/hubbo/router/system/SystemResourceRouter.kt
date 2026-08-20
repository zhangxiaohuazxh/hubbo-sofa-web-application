package cn.hubbo.router.system

import cn.hubbo.handler.system.SystemResourceHandler
import cn.hubbo.handler.test.TestHandler
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter

@Configuration
class SystemResourceRouter {

    /** 测试端点仅在配置 hubbo.test.enabled=true 的环境注册（生产环境禁用） */
    @Bean
    @ConditionalOnProperty(name = ["hubbo.test.enabled"], havingValue = "true")
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