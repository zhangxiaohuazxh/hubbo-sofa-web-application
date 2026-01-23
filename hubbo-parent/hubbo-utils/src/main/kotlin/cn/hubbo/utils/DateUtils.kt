package cn.hubbo.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DateUtils {

    companion object {

        @JvmStatic
        val datetimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        @JvmStatic
        fun currentTimeString(dateTime: LocalDateTime): String {
            return datetimeFormatter.format(dateTime)
        }


    }


}