package cn.hubbo.web.sys

import cn.dev33.satoken.stp.StpUtil
import cn.hubbo.auth.service.AuthenticationService
import cn.hubbo.entity.vo.ResultVO
import cn.hubbo.entity.vo.UserLoginParams
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/sys/auth")
class SysAuthenticationApi {


    companion object {
        @JvmStatic
        val logger: Logger = LoggerFactory.getLogger(SysAuthenticationApi::class.java)
    }


    val authenticationService: AuthenticationService? = null


    @PostMapping("/login")
    fun login(@RequestBody userLoginParams: UserLoginParams): ResultVO<Any> {
        logger.info("接收到的登录参数 {}", userLoginParams)
        StpUtil.login(userLoginParams.userId)
        val tokenValue = StpUtil.getTokenValue()
        return ResultVO.success(tokenValue)
    }


    @PostMapping("/validToken")
    fun validToken(@RequestHeader(name = "Authorization", required = true) headerValue: String): ResultVO<Any> {
        //  todo 验证token
        return ResultVO.success(true)
    }


    @PostMapping("/logout")
    fun logout(): ResultVO<Any> {
        StpUtil.logout()
        return ResultVO.success()
    }


}