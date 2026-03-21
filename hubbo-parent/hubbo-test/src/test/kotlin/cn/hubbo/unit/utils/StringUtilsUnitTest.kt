package cn.hubbo.unit.utils

import cn.hubbo.common.utils.StringUtils
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class StringUtilsUnitTest {

    private val logger: Logger by lazy { LoggerFactory.getLogger(StringUtilsUnitTest::class.java) }


    @Test
    fun testIsNumeric() {
        var res = StringUtils.isNumeric(byteArrayOf(49, 50, 51))
        logger.info("[49, 50, 51] isNumeric {}", res)
        res = StringUtils.isNumeric(byteArrayOf(0, 1, 2))
        logger.info("[0, 1, 2] isNumeric {}", res)
        res = StringUtils.isNumeric(byteArrayOf(11, 12, 49, 50, 51, 52), 2, 3)
        logger.info("[11, 12, 49, 50, 51, 52] isNumeric {}", res)
    }

}