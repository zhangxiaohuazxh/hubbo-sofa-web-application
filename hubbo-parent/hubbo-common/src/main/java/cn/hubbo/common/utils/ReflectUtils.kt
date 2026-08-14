package cn.hubbo.common.utils

// 注意：不能 import sun.misc.Unsafe —— kapt 会把源码里的 import 原样复制进生成的 Java 存根，
// 而 JDK 25 上 javac 对 sun.misc.Unsafe 的"内部专用 API"警告是强制警告（无法用 -nowarn /
// -Xlint / @SuppressWarnings 抑制），即使只是未使用的 import 也会触发。
// 因此这里不 import，只在方法体内用全限定名 sun.misc.Unsafe（stub 不包含方法体，不会产生警告）。
import java.lang.invoke.MethodHandles
import java.lang.ref.SoftReference
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

@Suppress("DEPRECATION", "DEPRECATION_ERROR", "UNCHECKED_CAST")
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

        // 注意：这里故意声明为 Any 而不是 Unsafe —— kapt 会为 Kotlin 声明生成 Java 存根并交给 javac 编译，
        // 而 JDK 25 上 javac 对 sun.misc.Unsafe 的"内部专用 API"警告是强制警告，无法用 -nowarn /
        // -Xlint / @SuppressWarnings 抑制。把 Unsafe 只留在方法体内（stub 不包含方法体），
        // 生成的存根就不会引用 sun.misc.Unsafe，也就不会产生这条每次编译都出现的警告。
        private val unsafe: Any by lazy {
            val declaredField = sun.misc.Unsafe::class.java.getDeclaredField("theUnsafe")
            declaredField.trySetAccessible()
            declaredField.get(null)!!
        }

        @JvmStatic
        fun lookup(): MethodHandles.Lookup {
            return lookup
        }

        @JvmStatic
        fun getUnsafeInstance(): Any {
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
            val offset = (unsafe as sun.misc.Unsafe).objectFieldOffset(field)
            objectFieldOffsetCache[key] = offset
            return offset
        }

        fun <T : Any> getObjectFieldValue(kClass: Class<T>, obj: Any, fieldName: String): Any {
            val fieldOffset = getObjectFieldOffset(kClass, fieldName)
            return (unsafe as sun.misc.Unsafe).getObject(obj, fieldOffset)
        }

        fun <T : Any> getObjectFieldValue(obj: Any, offset: Long): T {
            return (unsafe as sun.misc.Unsafe).getObject(obj, offset) as T
        }


    }

}
