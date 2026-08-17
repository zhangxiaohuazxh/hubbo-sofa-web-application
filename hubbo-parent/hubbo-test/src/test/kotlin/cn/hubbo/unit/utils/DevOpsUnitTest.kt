package cn.hubbo.unit.utils

import cn.hubbo.utils.DevOpsUtils
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object DevOpsUnitTest {

    private val logger: Logger by lazy { LoggerFactory.getLogger(DevOpsUnitTest::class.java) }

    @Test
    fun testParseRepositoryName() {
        val name = DevOpsUtils.parseRepositoryName("https://gitee.com/o-__-o/dev_tools.git")
        logger.info("解析出的仓库名称 {}", name)
    }


}