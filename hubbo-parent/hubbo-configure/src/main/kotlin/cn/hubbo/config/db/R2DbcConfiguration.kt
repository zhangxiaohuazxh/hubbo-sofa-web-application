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
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import java.time.Duration


@Configuration
class R2DbcConfiguration(val properties: DbProperties, private val environment: Environment) {

    private val sqlLogger: Logger by lazy { LoggerFactory.getLogger("cn.hubbo.JooqSQLLogger") }

    /** 生产环境关闭格式化/执行/诊断日志，减少不必要的开销 */
    private val productionProfile: Boolean = environment.acceptsProfiles(Profiles.of("prod"))

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
                }.onFailure { exception ->
                    sqlLogger.warn("捕获SQL日志失败", exception)
                }
            }
            .build()
    }


    @Bean("connectionPool")
    fun devConnectionPool(connectionFactory: ConnectionFactory): ConnectionPool? {
        val configuration = ConnectionPoolConfiguration.builder(connectionFactory)
            .maxIdleTime(Duration.ofSeconds(properties.poolMaxIdleTimeSeconds))
            .maxSize(properties.poolMaxSize)
            .build()
        return ConnectionPool(configuration)
    }


    @Bean
    fun dslContext(connectionPool: ConnectionPool): DSLContext {
        val settings = Settings().apply {
            withRenderSchema(false)
            withRenderCatalog(false)
            // 生产环境禁用
            withRenderFormatted(!productionProfile)
            withExecuteLogging(!productionProfile)
            // 生产环境禁用
            withDiagnosticsLogging(!productionProfile)
            withStatementType(StatementType.PREPARED_STATEMENT)
            // 阈值保持低值，避免过早内联绑定变量破坏 PostgreSQL 的查询计划缓存
            withInlineThreshold(3)
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
    /** 供 jOOQ 代码生成等 JDBC 场景使用 */
    val url: String,
    val poolMaxSize: Int = 10,
    val poolMaxIdleTimeSeconds: Long = 60,
) {

}