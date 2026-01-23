package cn.hubbo.contrast.json

import cn.hubbo.assist.domain.User
import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONFactory
import com.google.common.base.Stopwatch
import com.squareup.moshi.Moshi
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

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

        const val WARM_UP_TIMES = 1000

        val user = User("test")

        @JvmStatic
        @BeforeAll
        fun warmup(): Unit {
            JSONFactory.setDisableReferenceDetect(true)

            fastJsonWarmup()
            moshiJsonWarmup()
        }

        fun fastJsonWarmup() {
            val user = User("test")
            var str = ""
            for (i in 0 until WARM_UP_TIMES) {
                str = JSON.toJSONString(user)
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
        var stopwatch = Stopwatch.createStarted()
        val loopTimes = 10000
        var str = ""
        for (i in loopTimes downTo 0) {
            str = JSON.toJSONString(user)
        }
        var cost = stopwatch.elapsed(TimeUnit.MILLISECONDS)
        println("fastjson cost $cost ms")
        stopwatch = Stopwatch.createStarted()
        for (i in loopTimes downTo 0) {
            str = adapter.toJson(user)
        }
        cost = stopwatch.elapsed(TimeUnit.MILLISECONDS)
        println("moshi cost $cost ms")
    }


}