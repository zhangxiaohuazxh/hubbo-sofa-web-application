package cn.hubbo.common.log

import cn.hubbo.common.utils.DeSensitizationUtils.Companion.deSensitization
import com.google.common.base.Splitter
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.config.plugins.Plugin
import org.apache.logging.log4j.core.pattern.ConverterKeys
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter

@Plugin(name = "sensitiveDataTrimmer", category = "Converter")
@ConverterKeys("sdt")
class SensitiveDataTrimmer : LogEventPatternConverter {

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
        // 快速短路：手机号/证件号/银行卡必须含数字，邮箱必须含@，地址/姓名必然含非ASCII字符。
        // 三条都不满足时不可能命中任何脱敏规则，直接输出原文，避免每条日志都走分词+正则。
        if (!message.any { it.isDigit() } && !message.contains('@') && !message.any { it.code > 127 }) {
            buffer.append(message)
            return
        }
        val tokens = Splitter.on(" ").split(message)
        val iterator = tokens.iterator()
        var appended = false
        for (token in iterator) {
            if (appended) {
                buffer.append(' ')
            }
            buffer.append(deSensitization(token))
            appended = true
        }
    }


}