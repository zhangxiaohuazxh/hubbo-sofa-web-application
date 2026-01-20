package cn.hubbo.utils.extension

import cn.hubbo.utils.DateUtils
import java.time.LocalDateTime

fun LocalDateTime.currentTimeString(): String = DateUtils.currentTimeString(this)
