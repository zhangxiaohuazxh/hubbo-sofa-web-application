package cn.hubbo.integration.handler.test

import jakarta.annotation.Resource
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse

@SpringBootTest
class TestHandlerTest() {

    @Resource
    private lateinit var routerFunction: RouterFunction<ServerResponse>

    private val logger: Logger by lazy {
        LoggerFactory.getLogger(TestHandlerTest::class.java)
    }

    @Test
    fun testGetSystemTimeRouter(): Unit = runBlocking {
        val client: WebTestClient = WebTestClient.bindToRouterFunction(routerFunction).build()
        client.get()
            .uri("/test/datetime")
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBody<String>()
            .consumeWith {
                it.responseBody?.let {
                    logger.info("获取到的执行结果 {}", it)
                }
            }

    }

}
