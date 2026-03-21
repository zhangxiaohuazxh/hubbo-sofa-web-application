package cn.hubbo.unit.utils

import cn.hubbo.common.utils.DeSensitizationUtils.Companion.deSensitization
import cn.hubbo.common.utils.DeSensitizationUtils.Companion.getContentType
import org.apache.commons.lang3.StringUtils
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DeSensitizationUtilsUnitTest {

    private val logger: Logger by lazy { LoggerFactory.getLogger(DeSensitizationUtilsUnitTest::class.java) }

    @Test
    fun testGetContentType() {
        var contentType = getContentType("19912345600")
        logger.info("检测到19912345600的类型为 {}", contentType)
        contentType = getContentType("E12345678")
        logger.info("检测到E12345678的类型为 {}", contentType)
        contentType = getContentType("EA2345678")
        logger.info("检测到EA2345678的类型为 {}", contentType)
        contentType = getContentType("410225194910011234")
        logger.info("检测到410225194910011234的类型为 {}", contentType)
        contentType = getContentType("62131234123518")
        logger.info("检测到62131234123518的类型为 {}", contentType)
        contentType = getContentType("wantfulai@163.com")
        logger.info("检测到wantfulai@163.com的类型为 {}", contentType)
        contentType = getContentType("河南省郑州市金水东路22号")
        logger.info("检测到河南省郑州市金水东路22号的类型为 {}", contentType)
        contentType = getContentType("李思思")
        logger.info("检测到李思思的类型为 {}", contentType)
    }

    @Test
    fun testIsNumber() {
        logger.info("110 {}", StringUtils.isNumeric("110"))
        logger.info("110.00 {}", StringUtils.isNumeric("110.00"))
        logger.info("A12.00 {}", StringUtils.isNumeric("A12"))
    }

    @Test
    fun testDeSensitization() {
        var res = deSensitization("19912345600")
        logger.info("19912345600脱敏后的内容为 {}", res)
        res = deSensitization("E12345678")
        logger.info("E12345678脱敏后的内容为 {}", res)
        res = deSensitization("EA2345678")
        logger.info("EA2345678脱敏后的内容为 {}", res)
        res = deSensitization("410225194910011234")
        logger.info("410225194910011234脱敏后的内容为 {}", res)
        res = deSensitization("62131234123518")
        logger.info("62131234123518脱敏后的内容为 {}", res)
        res = deSensitization("wantfulai@163.com")
        logger.info("wantfulai@163.com脱敏后的内容为 {}", res)
        res = deSensitization("河南省郑州市金水东路22号")
        logger.info("河南省郑州市金水东路22号脱敏后的内容为 {}", res)
        res = deSensitization("李思思")
        logger.info("李思思脱敏后的内容为 {}", res)
    }

}