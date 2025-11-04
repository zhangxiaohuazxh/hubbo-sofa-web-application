package cn.hubbo.auth.service

import cn.hubbo.entity.vo.ResultVO
import cn.hubbo.entity.vo.UserVO
import com.alipay.sofa.runtime.api.annotation.SofaService
import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Service

@Slf4j
@SofaService(interfaceType = AuthenticationService::class)
@Service
class AuthenticationServiceImpl : AuthenticationService {

    override fun login(): ResultVO<UserVO> {
        TODO("Not yet implemented")
    }

}