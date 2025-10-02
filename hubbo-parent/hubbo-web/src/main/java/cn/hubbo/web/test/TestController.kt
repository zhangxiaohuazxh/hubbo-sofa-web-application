package cn.hubbo.web.test

import cn.hubbo.common.constants.LibraryConstants
import cn.hubbo.service.auth.AuthenticationService
import cn.hubbo.service.auth.UserService
import com.alipay.sofa.runtime.api.annotation.SofaReference
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.ZoneId

@RestController
@RequestMapping("/test")
class TestController {

    companion object {
        @JvmStatic
        val logger: Logger = LoggerFactory.getLogger(TestController::class.java)
    }

    @SofaReference(interfaceType = AuthenticationService::class)
    val authenticationService: AuthenticationService? = null

    @SofaReference(interfaceType = UserService::class)
    open val userService: UserService? = null

    @GetMapping("/sysdate")
    fun localDateTime(): LocalDateTime {
        val userInfo = userService!!.getById(1L)
        logger.info("查询到的用户信息 {}", userInfo?.toString())
        return LocalDateTime.now(ZoneId.of(LibraryConstants.DEFAULT_ZONE_ID.value))
    }


}
