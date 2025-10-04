package cn.hubbo.utils.context

import org.springframework.context.ApplicationContext
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.Environment

class ContextUtils {


    companion object {

        @JvmStatic
        private var application: ApplicationContext? = null

        @JvmStatic
        private var environment: Environment? = null

        @JvmStatic
        fun setApplicationContext(applicationContext: ApplicationContext) {
            lazy {
                application = applicationContext
                environment = application!!.environment
            }
        }

        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun <T> getBean(beanName: String): T {
            return application!!.getBean(beanName) as T
        }


        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun <T> getBean(jClass: Class<T>): T {
            return application!!.getBean(jClass) as T
        }

        @JvmStatic
        fun getEnvironment(): Environment {
            return environment!!
        }

        @JvmStatic
        fun closeApplication() {
            if (application is ConfigurableApplicationContext) {
                (application as ConfigurableApplicationContext).close()
            }
        }


    }


}