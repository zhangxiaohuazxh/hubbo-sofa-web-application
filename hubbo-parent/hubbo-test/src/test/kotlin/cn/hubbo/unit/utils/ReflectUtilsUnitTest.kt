package cn.hubbo.unit.utils

import cn.hubbo.common.utils.ReflectUtils
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ReflectUtilsUnitTest {

    private val logger: Logger by lazy { LoggerFactory.getLogger(ReflectUtilsUnitTest::class.java) }

    @Test
    fun testGetObjectFieldValue() {
        val str = "hello world"
        val handle = ReflectUtils.getObjectFieldHandle(String::class.java, "value")
        val value = ReflectUtils.getObjectFieldValue<ByteArray>(str, handle)
        logger.info("获取到的数组长度 {} 内容 {}", value.size, String(value))
    }


}