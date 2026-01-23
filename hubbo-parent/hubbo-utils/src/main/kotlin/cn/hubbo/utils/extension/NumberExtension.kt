package cn.hubbo.utils.extension

import kotlin.math.pow


fun Int.pow(power: Int): Int = this.toDouble().pow(power.toDouble()).toInt()

