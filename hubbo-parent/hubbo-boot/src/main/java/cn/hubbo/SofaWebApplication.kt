package cn.hubbo

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class SofaWebApplication {


    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(SofaWebApplication::class.java, *args)
        }

    }


}
