package cn.hubbo.web.test

import cn.hubbo.common.constants.LibraryConstants
import cn.hubbo.service.auth.RoleService
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

    @SofaReference
    open val roleService: RoleService? = null


    @GetMapping("/sysdate")
    fun localDateTime(): LocalDateTime {
        var role = roleService!!.getById(1L)
        print("role $role")
        return LocalDateTime.now(ZoneId.of(LibraryConstants.DEFAULT_ZONE_ID.value))
    }

}
