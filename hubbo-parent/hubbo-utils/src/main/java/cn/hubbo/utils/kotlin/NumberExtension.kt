package cn.hubbo.utils.kotlin

import kotlin.math.pow


fun Int.pow(power: Int): Int = this.toDouble().pow(power.toDouble()).toInt()

