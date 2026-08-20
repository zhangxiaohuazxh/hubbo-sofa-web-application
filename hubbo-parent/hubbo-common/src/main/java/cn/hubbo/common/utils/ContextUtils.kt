package cn.hubbo.common.utils

import org.springframework.context.ApplicationContext

class ContextUtils {

    companion object {

        private lateinit var applicationContext: ApplicationContext

        @JvmStatic
        fun setApplicationContext(applicationContext: ApplicationContext) {
            ContextUtils.applicationContext = applicationContext
        }

        @JvmStatic
        fun getApplicationContext(): ApplicationContext {
            if (::applicationContext.isInitialized) {
                return applicationContext
            }
            throw UnsupportedOperationException(
                "ApplicationContext not initialized. Call setApplicationContext() during application startup first."
            )
        }

    }


}