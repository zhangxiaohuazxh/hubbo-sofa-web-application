package cn.hubbo.task

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware


class ApplicationContextListener : ApplicationContextAware {

    private val logger: Logger by lazy { LoggerFactory.getLogger(ApplicationContextListener::class.java) }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        for (beanDefinitionName in applicationContext.beanDefinitionNames) {
            logger.info("bean definition name {}", beanDefinitionName)
        }
    }


}