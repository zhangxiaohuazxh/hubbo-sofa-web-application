package cn.hubbo.utils.coding

import com.google.common.collect.Maps

/**
 * FFI代理函数说明
 * 1. Mapper是一个正常的class,使用FFI注解标注该类
 * 2. 类型生成需要FFI执行的函数必须是native的，暂时不支持静态函数
 * 3. 函数返回值类型必须为primitive的，如int、double、long
 */
object MapperFactory {

    private val FFI_OBJECT_MAP: MutableMap<String?, Any?> = Maps.newHashMap<String?, Any?>()

    /**
     * 获取Mapper
     * 暂不考虑类加载器的影响
     *
     * @param `class` 类型
     * @param <T>    泛型
     * @return Mapper
     */
    fun <T> getMapper(`class`: Class<T?>): T? {
        return FFI_OBJECT_MAP[`class`.simpleName] as T?
    }

    fun <T> registerMapper(`class`: Class<T?>, mapper: T) {
        FFI_OBJECT_MAP[`class`.simpleName] = mapper
    }


}
