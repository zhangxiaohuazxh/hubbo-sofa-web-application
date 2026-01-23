package cn.hubbo.config.db

import io.r2dbc.pool.ConnectionPool
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile


//  todo r2dbc连接池配置
@Configuration
class DatasourceConfiguration {

    @Profile("dev")
    @Lazy
    @Bean("connectionPool")
    fun devConnectionPool(): ConnectionPool? {
        return null
    }


    @Profile("!dev")
    @Bean("connectionPool")
    fun connectionPool(): ConnectionPool? {
        return null
    }


}