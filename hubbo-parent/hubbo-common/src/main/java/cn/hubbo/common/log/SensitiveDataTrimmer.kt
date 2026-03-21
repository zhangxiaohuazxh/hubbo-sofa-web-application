package cn.hubbo.common.log

import cn.hubbo.common.utils.DeSensitizationUtils.Companion.deSensitization
import com.google.common.base.Splitter
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.config.plugins.Plugin
import org.apache.logging.log4j.core.pattern.ConverterKeys
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Plugin(name = "sensitiveDataTrimmer", category = "Converter")
@ConverterKeys("sdt")
class SensitiveDataTrimmer : LogEventPatternConverter {

    private val logger: Logger by lazy { LoggerFactory.getLogger(SensitiveDataTrimmer::class.java) }

    constructor(options: Array<String>?) : super("trim", "trim")

    companion object {

        @JvmStatic
        fun newInstance(options: Array<String>): SensitiveDataTrimmer {
            return SensitiveDataTrimmer(options)
        }

    }

    override fun format(event: LogEvent?, buffer: StringBuilder) {
        val message = event?.message?.formattedMessage ?: ""
        if (StringUtils.isBlank(message)) {
            return
        }
        val result = Splitter.on(" ").splitToStream(message)
            .map { deSensitization(it) }
            .toList()
            .joinToString("")
        buffer.append(result)
    }


}