package cn.hubbo.service.facade.auth

import cn.hubbo.dal.auth.ButtonPermissionMapper
import cn.hubbo.entity.auth.ButtonPermission
import com.alipay.sofa.runtime.api.annotation.SofaService
import com.mybatisflex.spring.service.impl.ServiceImpl
import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Service

@Slf4j
@SofaService
@Service
class ButtonPermissionService : ServiceImpl<ButtonPermissionMapper, ButtonPermission>() {


}