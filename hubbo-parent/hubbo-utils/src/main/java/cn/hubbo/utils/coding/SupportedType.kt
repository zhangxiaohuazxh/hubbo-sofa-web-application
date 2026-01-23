package cn.hubbo.utils.coding

import java.lang.foreign.ValueLayout

/**
 * 1.0版本之前不准备支持结构体和联合体
 */
enum class SupportedType(
    val typeName: String,
    val type: ValueLayout?
) {

    BYTE("byte", ValueLayout.JAVA_BYTE),

    SHORT("short", ValueLayout.JAVA_SHORT),

    INT("int", ValueLayout.JAVA_INT),

    LONG("long", ValueLayout.JAVA_LONG),

    FLOAT("float", ValueLayout.JAVA_FLOAT),

    DOUBLE("double", ValueLayout.JAVA_DOUBLE),

    BOOLEAN("boolean", ValueLayout.JAVA_BOOLEAN),

    CHAR("char", ValueLayout.JAVA_CHAR),

    /* 字符串以指针的形式返回 */
    STRING("String", ValueLayout.ADDRESS),

    /* 数组也以指针的形式返回 */
    ARRAY("array", ValueLayout.ADDRESS),

    /* 结构体需要单独处理 */
    STRUCT("struct", null),

    /* 联合体类型 */
    UNION("union", null)

    ;

    companion object {

        private val SUPPORTED_TYPES = setOf(
            "byte",
            "short",
            "int",
            "long",
            "float",
            "double",
            "boolean",
            "char",
            "string",
            "array",
            "struct",
            "union"
        )

        @JvmStatic
        fun of(typeName: String): SupportedType? {
            return when (typeName) {
                "byte" -> BYTE
                "short" -> SHORT
                "int" -> INT
                "long" -> LONG
                "float" -> FLOAT
                "double" -> DOUBLE
                "boolean" -> BOOLEAN
                "char" -> CHAR
                "string" -> STRING
                "array" -> ARRAY
                "struct" -> STRUCT
                "union" -> UNION
                else -> null
            }
        }

        @JvmStatic
        fun isSupportedType(typeName: String): Boolean {
            return typeName in SUPPORTED_TYPES
        }


    }

}
