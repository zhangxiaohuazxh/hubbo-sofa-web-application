package cn.hubbo.codegen

import cn.hubbo.config.db.DbProperties
import cn.hubbo.utils.NetUtils.Companion.isReachable
import jakarta.annotation.Resource
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.time.withTimeout
import org.jooq.codegen.GenerationTool
import org.jooq.meta.jaxb.*
import org.jooq.meta.jaxb.Target
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import java.io.File
import java.time.Duration

@SpringBootTest
class JooqCodeGenerate {

    private val logger: Logger by lazy { LoggerFactory.getLogger(JooqCodeGenerate::class.java) }

    @Resource
    private lateinit var properties: DbProperties

    companion object {

        private const val MODULE_NAME = "hubbo-dal"

        private const val RELATED_PATH = "src/main/kotlin"

        private const val PACKAGE_NAME = "cn.hubbo.dal"

        private val tables = listOf("t_user")

        private val includeTables = tables.joinToString(",")

        private val excludeTables = listOf("").joinToString(",")

        private const val SCHEMA = "public"

        private const val DRIVER_NAME = "org.postgresql.Driver"

        private const val DIALECT = "org.jooq.meta.postgres.PostgresDatabase"

    }


    @BeforeEach
    fun init() {
        System.clearProperty("all_proxy")
        if (!isReachable(properties.host, properties.port)) {
            System.setProperty("all_proxy", "socks5://127.0.0.1:1080")
            logger.warn("数据库服务器不可达，尝试使用网络代理连接")
        } else {
            logger.info("网络可达，取消代理设置")
        }
    }

    @Test
    fun testGetCurrentDir() {
        logger.info("当前目录 {}", getModulePath())
    }

    fun getModulePath(path: String = File(".").absolutePath, projectDir: String = "hubbo-parent"): String {
        val str = path.substring(path.lastIndexOf(File.separator) + 1)
        return if (str == projectDir) {
            path
        } else {
            getModulePath(File(path).parent!!)
        }
    }

    //    @Disabled
    @Test
    fun generate(): Unit = runBlocking {
        val module = "hubbo-dal"
        ::properties.isInitialized.takeIf { it }?.let {
            val res = withTimeout(Duration.ofMinutes(10)) {
                logger.info("开始生成代码")
                val jdbc = Jdbc()
                    .withUsername(properties.username)
                    .withUser(properties.username)
                    .withPassword(properties.password)
                    .withUrl(properties.url)
                    .withDriver(DRIVER_NAME)
                val database = Database().apply {
                    withName(DIALECT)
                    withIncludes(includeTables)
                    withExcludes(excludeTables)
                    withInputSchema(SCHEMA)
                }
                val target = Target().apply {
                    withPackageName(PACKAGE_NAME)
                    withDirectory("${getModulePath()}/$MODULE_NAME/$RELATED_PATH")
                }
                val generator = Generator().apply {
                    withName("org.jooq.codegen.KotlinGenerator")
                    withDatabase(database)
                    withTarget(target)
                    withGenerate(generateConfig())
                    withStrategy(strategy())
                }
                val conf = Configuration().apply {
                    withJdbc(jdbc)
                    withGenerator(generator)
                }
                //  也可以传xml文件路径，xml文件中不方便读取本地的配置参数
                GenerationTool.generate(conf)
                logger.info("生成代码成功")
                "success"
            }
            logger.info("最终执行结果 {}", res)
        } ?: run {
            logger.info("初始化配置参数失败")
        }
    }

    private fun generateConfig(): Generate = Generate().apply {
        withJavaTimeTypes(true)
        withInterfaces(true)
        withComments(true)
        withPojos(true)
        withDaos(true)
        withCommentsOnColumns(true)
        withImmutablePojos(false)
        withFluentSetters(true)
        withValidationAnnotations(true)
        withGeneratedAnnotation(true)
    }

    private fun strategy(): Strategy {
        return Strategy().apply {
            withName("org.jooq.codegen.DefaultGeneratorStrategy")
        }
    }

    @Test
    fun testCheckNetworkReachable() {
        val reachable = isReachable("127.0.0.1", 10808)
        logger.info("网络是否可达 {}", reachable)
    }


}
