package cn.hubbo.test.integration

import cn.hubbo.SofaWebApplication
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [SofaWebApplication::class])
class HubboIntegrationApplicationTest {

    companion object {

        //private val logger: Logger = LoggerFactory.getLogger(HubboIntegrationApplicationTest::class.java)

    }

    @Test
    fun demo01() {
        println("demo01")
    }
}
