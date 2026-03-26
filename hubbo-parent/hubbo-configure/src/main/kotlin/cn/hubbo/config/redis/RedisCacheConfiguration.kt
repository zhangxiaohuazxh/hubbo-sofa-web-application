package cn.hubbo.config.redis

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer


@Configuration
class RedisCacheConfiguration {


    @Bean
    fun reactiveRedisTemplate(connectionFactory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, Any> {
        val keySerializer = RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())
        val valueSerializer =
            RedisSerializationContext.SerializationPair.fromSerializer(JacksonJsonRedisSerializer(Any::class.java))
        val redisSerializationContext = RedisSerializationContext.newSerializationContext<String, Any>(keySerializer)
            .value(valueSerializer)
            .hashKey(keySerializer)
            .hashValue(valueSerializer)
            .build()
        return ReactiveRedisTemplate(connectionFactory, redisSerializationContext)
    }

}