package cn.hubbo.service.facade.auth

import cn.hubbo.entity.auth.Menu
import cn.hubbo.entity.vo.MenuVO
import cn.hubbo.service.auth.SystemMenuService
import cn.hubbo.service.mapper.SysAuthMapper
import com.alipay.sofa.runtime.api.annotation.SofaReference
import com.alipay.sofa.runtime.api.annotation.SofaService
import lombok.Data
import lombok.extern.slf4j.Slf4j
import org.jspecify.annotations.NonNull
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
        private val systemAuthMapper: SysAuthMapper by lazy { SysAuthMapper.INSTANCE }
    }


    override fun queryUserMenuList(): List<@NonNull MenuVO> {
        val menuList: List<Menu> = menuService!!.queryUserMenuList()
        val list = systemAuthMapper.menuList2MenuVOList(menuList.toList())
        return list.stream()
            .filter { it.upperMenuId == null || it.level == 1 }
            .distinct()
            .map { mapping(it, list, mutableSetOf()) }
            .toList()
    }

    fun mapping(menuVO: @NonNull MenuVO, menuList: @NonNull List<MenuVO>, visited: MutableSet<Long>): MenuVO {
        val menuId = menuVO.menuId
        if (menuId != null && !visited.add(menuId)) {
            menuVO.children = emptyList()
            return menuVO
        }
        menuVO.children = menuList.stream()
            .filter {
                it.upperMenuId?.equals(menuVO.menuId) == true
                        && it.menuId != null
                        && it.menuId?.equals(it.upperMenuId) == false
            }
            .map { mapping(it, menuList, visited) }
            .toList()
        return menuVO
    }

}
