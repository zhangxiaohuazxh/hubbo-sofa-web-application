package unit.extension

import cn.hubbo.utils.kotlin.isNumeric
import cn.hubbo.utils.kotlin.pow
import org.junit.jupiter.api.Test

class KotlinExtensionUnitTest {

    @Test
    fun testIntPow() {
        val num = 2
        println(2.pow(3))
    }

    @Test
    fun testIsNumeric() {
        val str = "123"
        println(str.isNumeric())
    }

}
