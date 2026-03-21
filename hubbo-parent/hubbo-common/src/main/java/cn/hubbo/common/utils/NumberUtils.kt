package cn.hubbo.common.utils

class NumberUtils {

    companion object {

        @JvmStatic
        fun isNumeric(value: Byte): Boolean {
            // 0..9
            return value in 48.toByte()..57.toByte()
        }

    }

}