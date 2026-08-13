package cn.hubbo.common.utils

import cn.hubbo.common.utils.DeSensitizationUtils.Type.*
import cn.hubbo.common.utils.StringUtils.Companion.isNumeric
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.regex.Pattern

class DeSensitizationUtils {

    companion object {

        private val logger: Logger by lazy { LoggerFactory.getLogger(DeSensitizationUtils::class.java) }

        private val emailPattern: Pattern by lazy { Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$") }

        /**
         * 数据脱敏
         * @param value 原始文本
         * @return 脱敏后的文本
         */
        @JvmStatic
        fun deSensitization(value: String): String {
            val type = getContentType(value)
            if (type == NONE) {
                return value
            }
            val chars = value.toCharArray()
            val asterisk = '*'
            return when (type) {
                PHONE -> {
                    String(
                        charArrayOf(
                            chars[0], chars[1], chars[2], asterisk, asterisk, asterisk, asterisk, chars[8], chars[9], chars[10]
                        )
                    )
                }

                VISA -> {
                    String(
                        charArrayOf(
                            chars[0], chars[1], chars[2], asterisk, asterisk, asterisk, asterisk, chars[7], chars[8]
                        )
                    )
                }

                ID_CARD -> {
                    String(
                        charArrayOf(
                            chars[0], chars[1], chars[2], chars[3], chars[4], chars[5],
                            asterisk,
                            asterisk,
                            asterisk,
                            asterisk,
                            asterisk,
                            asterisk,
                            asterisk,
                            asterisk,
                            chars[14],
                            chars[15],
                            chars[16],
                            chars[17],
                        )
                    )
                }

                UNION_PAY_BANK_CARD -> {
                    String(
                        charArrayOf(
                            chars[0], chars[1], chars[2], chars[3], chars[4], chars[5],
                            asterisk,
                            asterisk,
                            asterisk,
                            asterisk,
                            asterisk,
                            asterisk,
                            asterisk,
                            asterisk,
                            chars[chars.size - 5],
                            chars[chars.size - 4],
                            chars[chars.size - 3],
                            chars[chars.size - 2],
                            chars[chars.size - 1],
                        )
                    )
                }

                EMAIL -> {
                    val newChars = CharArray(chars.size)
                    for ((index, v) in chars.withIndex()) {
                        if (index <= 8) {
                            newChars[index] = asterisk
                        } else {
                            newChars[index] = v
                        }
                    }
                    String(newChars)
                }

                ADDRESS -> {
                    String(
                        charArrayOf(
                            chars[0], chars[1], chars[2], chars[3], chars[4], chars[5],
                            asterisk,
                            asterisk,
                            asterisk,
                            asterisk,
                            asterisk,
                            asterisk,
                            asterisk,
                            asterisk
                        )
                    )
                }

                NAME -> {
                    val newChars = CharArray(chars.size)
                    newChars[0] = chars[0]
                    for (index in 1 until chars.size) {
                        newChars[index] = asterisk
                    }
                    String(newChars)
                }

                NONE -> value
            }
        }

        @JvmStatic
        fun getContentType(value: String): Type {
            val len = value.length
            val chars = value.toCharArray()
            return when (len) {
                // 1\d{10} — 手机号
                11 if chars[0] == '1' && isNumeric(chars) -> {
                    PHONE
                }
                // 护照: E + 8位数字，或 E + 字母 + 7位数字
                9 if ((chars[0] == 'E' && isNumeric(chars, 1, chars.size)) ||
                    (chars[0] == 'E' && chars[1] in 'A'..'Z' && isNumeric(chars, 2, chars.size))
                ) -> {
                    VISA
                }
                // \d{17}[xX0-9] — 身份证
                18 if isNumeric(chars, 0, 16) &&
                    (chars[17].isDigit() || chars[17] == 'x' || chars[17] == 'X') -> {
                    ID_CARD
                }
                // 62开头 + 全数字 13~19位 — 银联卡
                in 13..19 if chars[0] == '6' && chars[1] == '2' && isNumeric(chars, 0, chars.size) -> {
                    UNION_PAY_BANK_CARD
                }
                // 邮箱
                in 8..25 if emailPattern.asPredicate().test(value) -> {
                    EMAIL
                }
                // 地址：关键词打分法（长度10~100）
                in 10..100 if StringUtils.isAddressString(value) -> {
                    ADDRESS
                }
                // 非ASCII字符（中文名等）：2~4字
                in 2..4 if value.any { it.code > 127 } -> {
                    NAME
                }

                else -> NONE
            }
        }


    }

    enum class Type {

        /* 手机号*/
        PHONE,

        /*护照*/
        VISA,

        /* 身份证号 */
        ID_CARD,

        /* 银联卡号 */
        UNION_PAY_BANK_CARD,

        /* 邮箱 */
        EMAIL,

        /* 住址信息 */
        ADDRESS,

        /*姓名 */
        NAME,

        /* 无需脱敏 */
        NONE,

    }

}
