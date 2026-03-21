package cn.hubbo.common.utils

class StringUtils {


    companion object {

        private val ADDRESS_KEYWORDS =
            arrayOf(
                "省",
                "市",
                "区",
                "县",
                "乡",
                "镇",
                "街",
                "路",
                "村",
                "楼",
                "单元",
                "大厦",
                "座",
                "栋",
                "东",
                "西",
                "南",
                "北",
                "经",
                "纬"
            )

        @JvmStatic
        fun isNumeric(array: ByteArray, start: Int = 0, end: Int = array.size): Boolean {
            for (index in start until end) {
                if (!NumberUtils.isNumeric(array[index])) {
                    return false
                }
            }
            return true
        }

        @JvmStatic
        fun isAddressString(value: String): Boolean {
            val chars = value.toCharArray()
            var score = 0
            for (keyword in ADDRESS_KEYWORDS) {
                if (value.contains(keyword)) {
                    score++
                }
            }
            return score >= 3 && chars.size >= 10
        }

    }


}