package cn.hubbo.common.utils

import java.lang.invoke.MethodHandles
import java.lang.invoke.VarHandle
import java.lang.ref.SoftReference
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/**
 * 反射工具：基于 VarHandle（JDK 9+）读写对象字段。
 *
 * 替代 sun.misc.Unsafe —— 后者是 JVM 内部 API，JDK 25 会触发强制警告，
 * 且绕过内存安全保证，存在被移除的风险。
 */
@Suppress("UNCHECKED_CAST")
class ReflectUtils {

    companion object {

        private val lookup: MethodHandles.Lookup by lazy { MethodHandles.lookup() }

        // 字段 VarHandle 缓存（只增不删，数量受类/字段规模约束，可控）
        private val varHandleCache: MutableMap<String, VarHandle> by lazy {
            ConcurrentHashMap(64)
        }

        private val objectMethodCache: Map<String, SoftReference<Method>> by lazy {
            ConcurrentHashMap(64)
        }

        @JvmStatic
        fun lookup(): MethodHandles.Lookup {
            return lookup
        }

        fun getObjectField(kClass: Class<out Any>, fieldName: String): Field {
            return kClass.getDeclaredField(fieldName)
        }

        /** 获取字段的 VarHandle（带缓存），用于高性能字段读写。 */
        @JvmStatic
        fun getObjectFieldHandle(kClass: Class<*>, fieldName: String): VarHandle {
            val key = "${kClass.name}-$fieldName"
            return varHandleCache.computeIfAbsent(key) {
                val field = getObjectField(kClass, fieldName)
                if (!field.trySetAccessible()) {
                    field.isAccessible = true
                }
                MethodHandles.privateLookupIn(kClass, lookup).unreflectVarHandle(field)
            }
        }

        /** 读取对象字段值（装箱形式）。 */
        @JvmStatic
        fun <T : Any> getObjectFieldValue(kClass: Class<T>, obj: Any, fieldName: String): Any {
            return getObjectFieldHandle(kClass, fieldName).get(obj)
        }

        /** 通过 [VarHandle] 读取对象字段值并转型。 */
        @JvmStatic
        fun <T : Any> getObjectFieldValue(obj: Any, handle: VarHandle): T {
            return handle.get(obj) as T
        }
    }

}