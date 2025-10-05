package cn.hubbo.service.auth

import cn.hubbo.dal.auth.MenuMapper
import cn.hubbo.entity.auth.Menu
import com.alipay.sofa.runtime.api.annotation.SofaService
import com.mybatisflex.spring.service.impl.ServiceImpl
import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Service

@Slf4j
@SofaService
@Service
class MenuService : ServiceImpl<MenuMapper, Menu>() {

}