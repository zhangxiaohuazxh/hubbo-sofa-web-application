package cn.hubbo.service.facade.auth

import cn.hubbo.dal.auth.MenuMapper
import cn.hubbo.entity.auth.Menu
import com.mybatisflex.spring.service.impl.ServiceImpl
import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Service

@Slf4j
@Service
class MenuService() : ServiceImpl<MenuMapper, Menu>() {

    fun queryUserMenuList(): List<Menu> {
        return mapper.selectUserMenuList()
    }

}