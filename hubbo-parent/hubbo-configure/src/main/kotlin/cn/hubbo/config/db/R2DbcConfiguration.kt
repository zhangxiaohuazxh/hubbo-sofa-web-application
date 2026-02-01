package cn.hubbo.config.db

import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.proxy.ProxyConnectionFactory
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions.*
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.conf.RenderQuotedNames
import org.jooq.conf.RenderTable
import org.jooq.conf.Settings
import org.jooq.conf.StatementType
import org.jooq.impl.DSL
import org.jooq.impl.DefaultConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration


@Configuration
class R2DbcConfiguration(val properties: DbProperties) {

    private val sqlLogger: Logger by lazy { LoggerFactory.getLogger("cn.hubbo.JooqSQLLogger") }

    @Bean
    fun connectionFactory(): ConnectionFactory {
        val connectionFactory = ConnectionFactories.get(
            builder()
                .option(DRIVER, properties.driver)
                .option(HOST, properties.host)
                .option(PORT, properties.port)
                .option(USER, properties.username)
                .option(PASSWORD, properties.password)
                .option(DATABASE, properties.dbname)
                .build()
        )
        // todo 优化点 某些情况下会报错
        return ProxyConnectionFactory.builder(connectionFactory)
            .onAfterQuery { queryExecutionInfo ->
                runCatching {
                    with(queryExecutionInfo) {
                        if (queryExecutionInfo.batchSize == 0) {
                            val queryInfo = queries[0]
                            queryInfo?.let {
                                val params = it.bindingsList.flatMap { bindings -> bindings.indexBindings }
                                    .mapNotNull { binding -> binding?.boundValue?.value }
                                    .map { v -> v.toString() }
                                sqlLogger.info("捕获到的sql {} {}", it.query, params)
                            }
                        } else {
                            queryExecutionInfo.queries.forEachIndexed { index, info ->
                                val params = info.bindingsList.flatMap { bindings -> bindings.indexBindings }
                                    .mapNotNull { binding -> binding?.boundValue?.value }
                                    .map { v -> v.toString() }
                                sqlLogger.info("捕获到的batch[{}] sql {} {}", index, info.query, params)
                            }
                        }
                    }
                }
            }
            .build()
    }


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
        val settings = Settings().apply {
            withRenderSchema(false)
            withRenderCatalog(false)
            // 线上环境禁用
            withRenderFormatted(true)
            withExecuteLogging(true)
            // 线上环境禁用
            withDiagnosticsLogging(true)
            withStatementType(StatementType.PREPARED_STATEMENT)
            withInlineThreshold(50)
            withRenderTable(RenderTable.WHEN_MULTIPLE_TABLES)
            withRenderQuotedNames(RenderQuotedNames.NEVER)
        }
        val configuration = DefaultConfiguration().apply {
            set(connectionPool)
            set(SQLDialect.POSTGRES)
            set(settings)
            // r2dbc模式下原有的监听器已经无法使用，只适用于JDBC的连接模式
            // set(DefaultExecuteListenerProvider(LoggerListener()))
        }
        return DSL.using(configuration)
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
