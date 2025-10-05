package cn.hubbo.service.auth

import cn.hubbo.dal.auth.RoleMapper
import cn.hubbo.entity.auth.Role
import com.alipay.sofa.runtime.api.annotation.SofaService
import com.mybatisflex.spring.service.impl.ServiceImpl
import org.springframework.stereotype.Service

@SofaService
@Service
open class RoleService : ServiceImpl<RoleMapper, Role>() {


}