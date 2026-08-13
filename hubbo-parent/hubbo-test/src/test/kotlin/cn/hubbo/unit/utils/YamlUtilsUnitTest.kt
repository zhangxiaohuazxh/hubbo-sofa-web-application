package cn.hubbo.unit.utils

import cn.hubbo.entity.devops.ci.BuildConfig
import cn.hubbo.utils.YamlUtils
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource

class YamlUtilsUnitTest {

    private val logger: Logger by lazy { LoggerFactory.getLogger(YamlUtilsUnitTest::class.java) }

    @Test
    fun testParseYamlConfig() {
        ClassPathResource("sample.yaml").inputStream.use {
            val res: Map<String, Any> = YamlUtils.parse(it)
            println("解析结果 \n ${res.toString()}")
        }
    }

    @Test
    fun testParseYamlConfig2SpecifiedType() {
        ClassPathResource("sample.yaml").inputStream.use {
            val res: BuildConfig = YamlUtils.parseAs(it)
            logger.info("解析的结果 {}", res)
        }
    }

}