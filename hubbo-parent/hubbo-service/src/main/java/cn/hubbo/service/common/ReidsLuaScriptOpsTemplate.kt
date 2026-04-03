package cn.hubbo.service.common

import org.springframework.data.redis.connection.ReactiveRedisConnection

interface ReidsLuaScriptOpsTemplate {


    /**
     * 上传lua脚本到redis服务器 返回一个脚本的sha1值
     * @param scriptContent lua脚本内容
     * @return 脚本的sha1值
     */
    suspend fun loadScript(scriptContent: String): String

    /**
     * 执行脚本内容
     * @param sha1 脚本的sha1值
     * @param keys 键列表
     * @param argv 参数列表
     * @param targetType 返回结果的类型
     * @return 返回执行结果
     * reids中调用lua脚本的两个具名参数 KEYS ARGV
     * 形式 eval "redis.call('SET',KEYS[1],ARGV[1]) return redis.call('GET',KEYS[1])" len key value
     * 这个len是keys这个table的长度，也即是元素的个数，redis会从这个len长度后取指定len个数的元素作为keys table，剩余的所有参数放入到argv table中
     * 这个形式和C语言的main函数签名类似 ARGV当做一个int*的指针处理即可
     */
    suspend fun <T : Any> evalSha(
        sha1: String,
        keys: MutableList<String>,
        argv: MutableList<*>,
        targetType: Class<T>
    ): T


    /**
     * 获取redis连接对象
     */
    fun getConnection(): ReactiveRedisConnection

}