package cn.hubbo.codegen

import cn.hubbo.config.db.DbProperties
import cn.hubbo.utils.NetUtils.Companion.isReachable
import jakarta.annotation.Resource
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.time.withTimeout
import org.apache.commons.lang3.StringUtils.isNotBlank
import org.jooq.codegen.DefaultGeneratorStrategy
import org.jooq.codegen.GenerationTool
import org.jooq.codegen.GeneratorStrategy
import org.jooq.meta.Definition
import org.jooq.meta.jaxb.*
import org.jooq.meta.jaxb.Target
import org.jooq.meta.postgres.PostgresTableDefinition
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import java.io.File
import java.time.Duration


private const val TABLE_PREFIX = "t_"

@SpringBootTest
class JooqCodeGenerate {

    private val logger: Logger by lazy { LoggerFactory.getLogger(JooqCodeGenerate::class.java) }

    @Resource
    private lateinit var properties: DbProperties

    companion object {

        private const val MODULE_NAME = "hubbo-dal"

        private const val RELATED_PATH = "src\\main\\kotlin"

        private const val PACKAGE_NAME = "cn.hubbo.dal"

        //  生成完之后不要删除已经生成过的表，否则生成之后文件会被覆盖
        private val tables = listOf(
            "t_user",
            "t_role",
            "t_user_role",
            "t_menu",
            "t_permission",
            "t_menu_permission",
            "t_role_permission",
            "t_dict_data",
            "t_button_permission",
            "t_project",
            "t_project_iteration"
        )

        private val excludeTables = null

        private const val SCHEMA = "public"

        private const val DRIVER_NAME = "org.postgresql.Driver"

        private const val DIALECT = "org.jooq.meta.postgres.PostgresDatabase"

        private const val DEFAULT_STRATEGY: Boolean = true

    }


    @BeforeEach
    fun init() {
        logger.info("properties信息 {}", properties)
        System.clearProperty("all_proxy")
        if (!isReachable(properties.host, properties.port)) {
            System.setProperty("all_proxy", "socks5://127.0.0.1:1080")
            System.setProperty("socksProxyHost", "127.0.0.1")
            System.setProperty("socksProxyPort", "1080")
            System.setProperty("socksProxyVersion", "5")
            logger.warn("数据库服务器不可达，已设置 SOCKS5 代理连接: 127.0.0.1:1080")
            logger.info("再次检测网络是否可达 {}", isReachable(properties.host, properties.port))
        } else {
            logger.info("网络可达，取消代理设置")
        }
    }

    @Disabled
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

    //        @Disabled
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
                    withIncludes(tables.joinToString("|", "^(", postfix = ")$"))
                    withExcludes(excludeTables)
                    withInputSchema(SCHEMA)
                }
                val target = Target().apply {
                    withPackageName(PACKAGE_NAME)
                    withDirectory("${getModulePath()}\\$MODULE_NAME\\$RELATED_PATH")
                    withClean(false)
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
        // 启用Java 8时间类型支持 (LocalDate, LocalTime等)
        withJavaTimeTypes(true)
        // 生成POJO类
        withPojos(true)
        // 不生成不可变POJOs (即POJO不包含setter方法)
        withImmutablePojos(true)
        // 启用流畅setter模式 (setter方法返回this以支持链式调用)
        withFluentSetters(true)
        //  data class 而不是 class
        withPojosAsKotlinDataClasses(true)
        //  JSR-303数据校验注解
        withValidationAnnotations(true)
        // 生成@Generated注解标记代码生成工具
        withGeneratedAnnotation(true)
        //  是否生成接口
        withInterfaces(true)
        //  生成索引信息
        withIndexes(true)
        //  jpa注解
        withJpaAnnotations(false)
        //  添加spring注解
        withSpringAnnotations(true)
        //  生成spring风格dao
        withSpringDao(false)
        //  生成dao实现
        withDaos(false)
        // 为非空列生成非空POJO属性
        withKotlinNotNullPojoAttributes(true)
        // Kotlin: 为非空列生成非空Record属性
        withKotlinNotNullRecordAttributes(true)
        withKotlinNotNullInterfaceAttributes(true)
        // 生成字段、方法和类的注释
        withComments(true)
        // 生成表级注释
        withCommentsOnTables(true)
        // 生成列级注释
        withCommentsOnColumns(true)
        // 生成属性级注释
        withCommentsOnAttributes(true)
        // 生成包级注释
        withCommentsOnPackages(true)
        // 为数组类型生成vararg setter方法以方便使用
        withVarargSetters(true)
    }

    private fun strategy(): Strategy {
        return Strategy().apply {
            if (DEFAULT_STRATEGY) {
                withName("org.jooq.codegen.DefaultGeneratorStrategy")
            } else {
                withName("cn.hubbo.codegen.CustomRenamingStrategy")
            }
        }
    }

    @Test
    fun testCheckNetworkReachable() {
        val reachable = isReachable("127.0.0.1", 10808)
        logger.info("网络是否可达 {}", reachable)
    }

}

class CustomRenamingStrategy() : DefaultGeneratorStrategy() {
    override fun getJavaClassName(definition: Definition?, mode: GeneratorStrategy.Mode?): String? {
        val className = super.getJavaClassName(definition, mode)
        return if (definition != null
            && definition is PostgresTableDefinition
            && isNotBlank(className)
            && definition.name?.startsWith(TABLE_PREFIX) == true
        ) {
            className.substring(1)
        } else className
    }
}