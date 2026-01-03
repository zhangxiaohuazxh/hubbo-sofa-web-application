package cn.hubbo.configure.web

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class HubboWebConfiguration {

    @Value(value = $$"${registry.endpoint}")
    private val endpoint: String? = null

    @Value($$"${registry.endpointPort}")
    private val endpointPort: Int = 9063


}