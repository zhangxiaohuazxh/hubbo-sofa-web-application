package cn.hubbo.common.utils

import cn.hubbo.common.utils.DeSensitizationUtils.Type.*
import cn.hubbo.common.utils.StringUtils.Companion.isNumeric
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles.privateLookupIn
import java.lang.invoke.VarHandle
import java.util.regex.Pattern

class DeSensitizationUtils {

    companion object {

        private val logger: Logger by lazy { LoggerFactory.getLogger(DeSensitizationUtils::class.java) }

        private val emailPattern: Pattern by lazy { Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$") }

        private val stringCoderVarHandle: VarHandle by lazy {
            val methodHandlesLookup = ReflectUtils.lookup()
            return@lazy privateLookupIn(String::class.java, methodHandlesLookup).findVarHandle(
                String::class.java, "coder", Byte::class.javaPrimitiveType
            )
        }

        private val stringByteArrayVarHandle: VarHandle by lazy {
            val methodHandlesLookup = ReflectUtils.lookup()
            return@lazy privateLookupIn(String::class.java, methodHandlesLookup).findVarHandle(
                String::class.java, "value", ByteArray::class.java
            )
        }

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
            val arr: ByteArray = stringByteArrayVarHandle.get(value) as ByteArray
            val asterisk = 42.toByte()
            return when (type) {
                PHONE -> {
                    return String(
                        byteArrayOf(
                            arr[0], arr[1], arr[2], asterisk, asterisk, asterisk, asterisk, arr[8], arr[9], arr[10]
                        )
                    )
                }

                VISA -> {
                    return String(
                        byteArrayOf(
                            arr[0], arr[1], arr[2], asterisk, asterisk, asterisk, asterisk, arr[7], arr[8]
                        )
                    )
                }

                ID_CARD -> {
                    return String(
                        byteArrayOf(
                            arr[0], arr[1], arr[2], arr[3], arr[4], arr[5],
                            asterisk,
                            asterisk,
                            asterisk,
                            asterisk,
                            asterisk,
                            asterisk,
                            asterisk,
                            asterisk,
                            arr[14],
                            arr[15],
                            arr[16],
                            arr[17],
                        )
                    )
                }

                UNION_PAY_BANK_CARD -> {
                    return String(
                        byteArrayOf(
                            arr[0], arr[1], arr[2], arr[3], arr[4], arr[5],
                            asterisk,
                            asterisk,
                            asterisk,
                            asterisk,
                            asterisk,
                            asterisk,
                            asterisk,
                            asterisk,
                            arr[arr.size - 5],
                            arr[arr.size - 4],
                            arr[arr.size - 3],
                            arr[arr.size - 2],
                            arr[arr.size - 1],
                        )
                    )
                }

                EMAIL -> {
                    val newArray = ByteArray(arr.size)
                    for ((index, v) in arr.withIndex()) {
                        if (index <= 8) {
                            newArray[index] = asterisk
                        } else {
                            newArray[index] = v
                        }
                    }
                    return String(newArray)
                }

                ADDRESS -> {
                    val chars = value.toCharArray()
                    val asterisk = 42.toChar()
                    return String(
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
                    val chars = value.toCharArray()
                    val asterisk = 42.toChar()
                    val newChars = CharArray(chars.size)
                    newChars[0] = chars[0]
                    for (index in 1 until chars.size) {
                        newChars[index] = asterisk
                    }
                    return String(newChars)
                }

                NONE -> value
            }
        }

        @JvmStatic
        fun getContentType(value: String): Type {
            val latin1 = stringCoderVarHandle.get(value) == 0.toByte()
            val len = value.length
            val array: ByteArray = stringByteArrayVarHandle.get(value) as ByteArray
            return when (len) { // 1\\d{10}
                11 if latin1 && array[0] == 49.toByte() && isNumeric(array) -> {
                    PHONE
                } // 2017年4月以前签发的电子普通护照号码由“E”+8位阿拉伯数字组成。正如你所能预见的，当签发到第100,000,000本电子护照时，
                // 2017年4月5日起新的电子普通护照号码生成规则变为：第一位仍是前缀字母“E”，第二位改为顺序使用英文字母（“I”、“O”除外），第三位起7位阿拉伯数字，护照号码总位数仍为9位。
                // 第二位改为顺序使用英文字母（“I”、“O”除外）需要严谨校验的可以加上，这里就忽略掉了
                9 if latin1 && ((array[0] == 69.toByte() && isNumeric(
                    array, 1, array.size
                )) || (array[0] == 69.toByte() && array[1] in 65.toByte()..90.toByte() && isNumeric(
                    array, 2, array.size
                ))) -> {
                    VISA
                } // \\d{17}[x0-9]{1}最后一位允许是字母x
                18 if latin1 && isNumeric(
                    array, 0, 16
                ) && (NumberUtils.isNumeric(array[17]) || array[17] == 120.toByte() || array[17] == 88.toByte()) -> {
                    ID_CARD
                } // 62开头 62\\d{14}
                in 13..19 if latin1 && array[0] == 54.toByte() && array[1] == 50.toByte() && isNumeric(
                    array, 0, array.size
                ) -> {
                    UNION_PAY_BANK_CARD
                }

                in 8..25 if (latin1 && emailPattern.asPredicate().test(value)) -> {
                    EMAIL
                }

                // 地址不好判断，不再使用正则，使用关键词打分法
                in 10..100 if (StringUtils.isAddressString(value)) -> {
                    ADDRESS
                } // 不准备支持英文名称 所以直接判断非latin1编码的字符串
                in 2..4 if !latin1 -> {
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
