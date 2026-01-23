package cn.hubbo.apt.annotation.processor

import java.lang.reflect.Type

enum class SupportTypes(val type: Type) {

    BYTE(Byte::class.java),
    SHORT(Short::class.java),
    INT(Int::class.java),
    LONG(Long::class.java),
    FLOAT(Float::class.java),
    DOUBLE(Double::class.java),
    BOOLEAN(Boolean::class.java),
    CHAR(Char::class.java),
    STRING(String::class.java),
    ARRAY(Array<Any>::class.java),
    ;

    companion object {
        fun getSupportType(type: Type): SupportTypes? {
            for (supportType in entries) {
                if (supportType.type === type) {
                    return supportType
                }
            }
            return null
        }
    }

}