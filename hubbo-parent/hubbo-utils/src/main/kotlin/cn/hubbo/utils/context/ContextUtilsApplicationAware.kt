package cn.hubbo.utils.context

import cn.hubbo.utils.fory.ForyComponentRegisterManager
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

class ContextUtilsApplicationAware : ApplicationContextAware {


    override fun setApplicationContext(applicationContext: ApplicationContext) {
        ContextUtils.setApplicationContext(applicationContext)
    }


}
