package cn.hubbo.common.constants

enum class LibraryConstants(val value: String) {

    /* 默认市区 */
    DEFAULT_ZONE_ID("Asia/Shanghai"),

    /* trace id的key */
    TRACE_ID("traceId"),

    /* native库名称，见hubbo-native模块的配置 */
    HUBBO_DYNAMIC_LIBRARY_NAME("hubbo_native_dynamic_lib"),

    /* native库的后缀名 */
    HUBBO_DYNAMIC_LIBRARY_PATH("lib"),

    /* 请求头中的trace id */
    TRACE_ID_HEADER("X-Trace-Id"),

    /* 协程中上下文存储数据的map 用来替代threadLocal,不过这个map只能存字符串 */
    COROUTINES_CONTEXT_KEY("COROUTINES_CONTEXT"),

}
