package cn.hubbo.service.facade.auth

import cn.hubbo.dal.auth.PermissionMapper
import cn.hubbo.entity.auth.Permission
import com.mybatisflex.spring.service.impl.ServiceImpl
import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Service

@Slf4j
@Service
class PermissionService : ServiceImpl<PermissionMapper, Permission>() {

}