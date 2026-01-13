package unit.contrast

import com.alibaba.fastjson2.JSON.toJSONString
import com.google.common.base.Stopwatch.createStarted
import com.squareup.moshi.Moshi
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import unit.domain.User
import java.util.concurrent.TimeUnit.MILLISECONDS

class JsonUnitTest {

    /**
     * fastjson
     *
     * moshi
     *
     * 中位数对比：
     * 平均数对比：
     */

    companion object {

        val moshi = Moshi.Builder().build()!!

        const val WARM_UP_TIMES = 10000

        val user = User("test")

        @JvmStatic
        @BeforeAll
        fun warmup(): Unit {
            fastJsonWarmup()
            moshiJsonWarmup()
        }

        fun fastJsonWarmup() {
            val user = User("test")
            var str = ""
            for (i in 0 until WARM_UP_TIMES) {
                str = toJSONString(user)
            }
            println(str)
        }

        fun moshiJsonWarmup() {
            val user = User("test")
            val adapter = moshi.adapter<User>(User::class.java)
            var str = ""
            for (i in 0 until WARM_UP_TIMES) {
                str = adapter.toJson(user)
            }
            println(str)
        }

    }

    @Test
    fun testFastJsonAndMoshi() {
        val adapter = moshi.adapter<User>(User::class.java)
        var stopwatch = createStarted()
        val loopTimes = 100000
        var str = ""
        for (i in loopTimes downTo 0) {
            str = toJSONString(user)
        }
        var cost = stopwatch.elapsed(MILLISECONDS)
        println("fastjson cost $cost ms")
        stopwatch = createStarted()
        for (i in loopTimes downTo 0) {
            str = adapter.toJson(user)
        }
        cost = stopwatch.elapsed(MILLISECONDS)
        println("moshi cost $cost ms")
    }


}

