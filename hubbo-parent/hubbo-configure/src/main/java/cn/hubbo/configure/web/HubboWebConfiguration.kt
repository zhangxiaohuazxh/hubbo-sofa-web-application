package cn.hubbo.configure.web

import cn.hubbo.utils.fory.ForyComponentRegisterManager
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.apache.fory.Fory
import org.apache.fory.ThreadLocalFory
import org.apache.fory.config.Language
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Configuration
class HubboWebConfiguration {

    @Value(value = $$"${registry.endpoint}")
    private val endpoint: String? = null

    @Value($$"${registry.endpointPort}")
    private val endpointPort: Int = 9063


    @Bean
    fun httpMessageConvert(): HttpMessageConverter<Any> {
        val httpMessageConverter = MappingJackson2HttpMessageConverter()
        val objectMapper = ObjectMapper()
        val pattern = "yyyy-MM-dd HH:mm:ss"
        val dateTimeFormatter = DateTimeFormatter.ofPattern(pattern)
        objectMapper.dateFormat = SimpleDateFormat(pattern)
        objectMapper.registerModule(KotlinModule.Builder().build())
        val javaTimeModule = JavaTimeModule()
        javaTimeModule.addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer(dateTimeFormatter))
        javaTimeModule.addDeserializer(LocalDateTime::class.java, LocalDateTimeDeserializer(dateTimeFormatter))
        objectMapper.registerModule(javaTimeModule)
        httpMessageConverter.objectMapper = objectMapper
        httpMessageConverter.defaultCharset = Charset.forName("UTF-8")
        return httpMessageConverter
    }

    @Bean
    fun fory(): ThreadLocalFory {
        val fory = Fory.builder().requireClassRegistration(true)
            .registerGuavaTypes(true)
            .suppressClassRegistrationWarnings(true)
            .withLanguage(Language.JAVA)
            .withLanguage(Language.JAVASCRIPT)
            .buildThreadLocalFory()
        ForyComponentRegisterManager.register(fory)
        return fory
    }

}