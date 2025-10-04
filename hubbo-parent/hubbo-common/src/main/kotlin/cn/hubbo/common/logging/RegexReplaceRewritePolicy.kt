package cn.hubbo.common.logging

import cn.hubbo.common.constants.HubboEnumConstants
import org.apache.logging.log4j.core.Core
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.appender.rewrite.RewritePolicy
import org.apache.logging.log4j.core.config.plugins.Plugin
import org.apache.logging.log4j.core.config.plugins.PluginFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Plugin(
    name = "RegexReplaceRewritePolicy",
    category = Core.CATEGORY_NAME,
    elementType = "rewritePolicy",
    printObject = true
)
open class RegexReplaceRewritePolicy : RewritePolicy {

    companion object {

        private val LOGGER: Logger = LoggerFactory.getLogger(RegexReplaceRewritePolicy::class.java)

        @PluginFactory
        @JvmStatic
        fun createPolicy(): RegexReplaceRewritePolicy {
            return RegexReplaceRewritePolicy()
        }

    }

    override fun rewrite(source: LogEvent): LogEvent? {
        if (!source.loggerName.startsWith(HubboEnumConstants.ROOT_PACKAGE_NAME.value)) {
            return source
        }
        return source
    }


}
