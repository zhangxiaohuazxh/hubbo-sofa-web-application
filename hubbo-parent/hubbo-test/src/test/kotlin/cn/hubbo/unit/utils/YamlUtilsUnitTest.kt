package cn.hubbo.unit.utils

import cn.hubbo.utils.YamlUtils
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource

class YamlUtilsUnitTest {


    @Test
    fun testParseYamlConfig() {
        ClassPathResource("sample.yaml").inputStream.use {
            val res: Map<String, Any> = YamlUtils.parse(it)
            println("解析结果 \n ${res.toString()}")
        }
    }

}