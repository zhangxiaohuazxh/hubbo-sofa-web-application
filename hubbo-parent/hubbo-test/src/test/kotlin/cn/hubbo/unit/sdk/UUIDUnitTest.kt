package cn.hubbo.unit.sdk

import org.junit.jupiter.api.Test
import java.util.UUID.randomUUID

class UUIDUnitTest {

    @Test
    fun testRandomUUID() {
        // uuid v4 jdk26才有的v7版本
        // todo 是否升级jdk26
        val str = randomUUID().toString()
        println(str)
    }

}