package cn.hubbo.common.utils

import sun.misc.Unsafe
import java.lang.invoke.MethodHandles
import java.lang.ref.SoftReference
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

class ReflectUtils {

    companion object {

        private val lookup: MethodHandles.Lookup by lazy { MethodHandles.lookup() }

        // 不强制缓存，内存不足时允许释放掉缓存对象
        private val objectFieldOffsetCache: MutableMap<String, Long> by lazy {
            ConcurrentHashMap(64)
        }

        private val objectMethodCache: Map<String, SoftReference<Method>> by lazy {
            ConcurrentHashMap(64)
        }

        private val unsafe: Unsafe by lazy {
            val declaredField = Unsafe::class.java.getDeclaredField("theUnsafe")
            declaredField.trySetAccessible()
            declaredField.get(null) as Unsafe
        }

        @JvmStatic
        fun lookup(): MethodHandles.Lookup {
            return lookup
        }

        @JvmStatic
        fun getUnsafeInstance(): Unsafe {
            return unsafe
        }

        fun getObjectField(kClass: Class<out Any>, fieldName: String): Field {
            return kClass.getDeclaredField(fieldName)
        }


        fun <T : Any> getObjectFieldOffset(kClass: Class<T>, fieldName: String): Long {
            val key = "${kClass.name}-$fieldName"
            if (objectFieldOffsetCache.containsKey(key)) {
                return objectFieldOffsetCache[key]!!
            }
            val field = getObjectField(kClass, fieldName)
            val offset = unsafe.objectFieldOffset(field)
            objectFieldOffsetCache[key] = offset
            return offset
        }

        fun <T : Any> getObjectFieldValue(kClass: Class<T>, obj: Any, fieldName: String): Any {
            val fieldOffset = getObjectFieldOffset(kClass, fieldName)
            return unsafe.getObject(obj, fieldOffset)
        }

        fun <T : Any> getObjectFieldValue(obj: Any, offset: Long): T {
            return unsafe.getObject(obj, offset) as T
        }


    }

}