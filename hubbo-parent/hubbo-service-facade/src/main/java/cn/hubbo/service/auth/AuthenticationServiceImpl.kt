package cn.hubbo.service.auth

import com.alipay.sofa.runtime.api.annotation.SofaReference
import com.alipay.sofa.runtime.api.annotation.SofaService
import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Service

@Slf4j
@SofaService(interfaceType = AuthenticationService::class)
@Service
class AuthenticationServiceImpl : AuthenticationService {

    @SofaReference
    open val userService: UserService? = null

    @SofaReference
    open val roleService: RoleService? = null

    @SofaReference
    open val menuService: MenuService? = null

    @SofaReference
    open val permissionService: PermissionService? = null

    @SofaReference
    open val buttonPermissionService: ButtonPermissionService? = null


}
