package cn.hubbo.service.auth

import cn.hubbo.dal.auth.UserMapper
import cn.hubbo.entity.auth.User
import com.alipay.sofa.runtime.api.annotation.SofaService
import com.mybatisflex.spring.service.impl.ServiceImpl
import org.springframework.stereotype.Service

@SofaService(interfaceType = UserService::class)
@Service
open class UserServiceImpl : UserService, ServiceImpl<UserMapper, User>() {


}