package cn.hubbo.service.auth

import cn.hubbo.entity.auth.Menu
import cn.hubbo.entity.vo.MenuVO
import cn.hubbo.service.mapper.SysAuthMapper
import com.alipay.sofa.runtime.api.annotation.SofaReference
import com.alipay.sofa.runtime.api.annotation.SofaService
import lombok.Data
import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Service

@Slf4j
@SofaService(interfaceType = SystemMenuService::class)
@Service
@Data
class SystemMenuServiceImpl : SystemMenuService {

    @SofaReference
    private val menuService: MenuService? = null

    @SofaReference
    private val roleService: RoleService? = null

    @SofaReference
    private val permissionService: PermissionService? = null

    @SofaReference
    private val userService: UserService? = null

    @SofaReference
    private val buttonPermissionService: ButtonPermissionService? = null

    companion object {
        @JvmStatic
        private val systemAuthMapper: SysAuthMapper = SysAuthMapper.INSTANCE
    }


    override fun queryUserMenuList(): List<MenuVO> {
        val menuList: List<Menu> = menuService!!.queryUserMenuList()
        return systemAuthMapper.menuList2MenuVOList(menuList.toList())
    }


}
