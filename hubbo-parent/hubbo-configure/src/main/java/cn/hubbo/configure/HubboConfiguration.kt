package cn.hubbo.configure

import cn.hubbo.utils.context.ContextUtilsApplicationAware
import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy

@EnableAutoConfiguration
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("cn.hubbo.dal")
@Configuration
open class HubboConfiguration {


    @Bean
    open fun contextUtilsApplicationAware(): ContextUtilsApplicationAware {
        return ContextUtilsApplicationAware()
    }


}
