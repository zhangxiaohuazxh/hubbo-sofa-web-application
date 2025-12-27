package cn.hubbo.service.facade.auth

import cn.hubbo.dal.auth.RoleMapper
import cn.hubbo.entity.auth.Role
import com.mybatisflex.spring.service.impl.ServiceImpl
import org.springframework.stereotype.Service

@Service
open class RoleService : ServiceImpl<RoleMapper, Role>() {


}