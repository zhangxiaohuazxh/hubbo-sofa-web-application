package cn.hubbo.config.db

import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions.*
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.time.Duration


@Configuration
class R2DbcConfiguration(val properties: DbProperties) {

    @Bean
    fun connectionFactory(): ConnectionFactory {
        return ConnectionFactories.get(
            builder()
                .option(DRIVER, properties.driver)
                .option(HOST, properties.host)
                .option(PORT, properties.port)
                .option(USER, properties.username)
                .option(PASSWORD, properties.password)
                .option(DATABASE, properties.dbname)
                .build()
        )
    }

    @Profile("dev")
    @Bean("connectionPool")
    fun devConnectionPool(): ConnectionPool? {
        val configuration = ConnectionPoolConfiguration.builder(connectionFactory())
            .maxIdleTime(Duration.ofMinutes(1))
            .maxSize(10)
            .build()
        return ConnectionPool(configuration)
    }


    @Bean
    fun dslContext(connectionPool: ConnectionPool): DSLContext {
        return DSL.using(connectionPool, SQLDialect.POSTGRES)
    }

}

@ConfigurationProperties(prefix = "r2dbc.datasource")
class DbProperties(
    val driver: String,
    val host: String,
    val port: Int,
    val dbname: String,
    val username: String,
    val password: String,
    val url: String
) {

}
