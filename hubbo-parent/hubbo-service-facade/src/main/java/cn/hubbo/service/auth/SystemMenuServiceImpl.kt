package cn.hubbo.service.auth;

import cn.hubbo.entity.auth.Menu;
import com.alipay.sofa.runtime.api.annotation.SofaService;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@SofaService(interfaceType = SystemMenuService.class)
@Service
@RequiredArgsConstructor
@Data
public class SystemMenuServiceImpl implements SystemMenuService {

    @NonNull
    private MenuService menuService;

    @NonNull
    private RoleService roleService;

    @NonNull
    private PermissionService permissionService;

    @NonNull
    private UserService userService;

    @NonNull
    private ButtonPermissionService buttonPermissionService;


    @Override
    public List<Menu> queryMenuList() {
        return List.of();
    }




}
