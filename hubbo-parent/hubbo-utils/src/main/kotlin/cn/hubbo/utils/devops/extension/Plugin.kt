package cn.hubbo.utils.devops.extension

import kotlin.reflect.KClass

/**
 * 插件。
 *
 * 通过 [PluginRegistry] 注册钩子、覆盖能力实现，实现「工具可插拔」。
 * 例如：自定义 Linter 适配器、自定义通知渠道、私有部署策略等。
 *
 * 约定：[apply] 只做同步注册，不执行 I/O；重型初始化应延迟到能力被调用时。
 */
interface Plugin {
    val name: String
    val version: String get() = "1.0.0"

    fun apply(registry: PluginRegistry)
}

/**
 * 插件注册表。
 *
 * 能力绑定（[bind] / [resolve]）采用类型键，插件可将更细粒度的能力实现
 * 绑定到接口上，运行时以「最后绑定者胜出」覆盖默认实现。
 */
interface PluginRegistry {
    val hooks: HookRegistry

    fun <T : Any> bind(type: Class<T>, instance: T)
    fun <T : Any> resolve(type: Class<T>): T?

    fun <T : Any> bind(type: KClass<T>, instance: T) = bind(type.java, instance)
    fun <T : Any> resolve(type: KClass<T>): T? = resolve(type.java)
}
