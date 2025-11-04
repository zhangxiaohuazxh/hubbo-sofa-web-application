package cn.hubbo.configure.shared

import io.netty.handler.ssl.SslContextBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
class SharedComponentConfiguration {


    @Bean
    fun httpClient(): HttpClient {
        return HttpClient.create()
            .responseTimeout(Duration.ofMinutes(1))
            .wiretap(true)
            .compress(true)
            .keepAlive(true)
            .followRedirect(true)
            .secure {
                it.sslContext(SslContextBuilder.forClient().build())
            }
    }

}
