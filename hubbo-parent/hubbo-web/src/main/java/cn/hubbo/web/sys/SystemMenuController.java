package cn.hubbo.web.sys;

import cn.hubbo.entity.vo.ResultVO;
import cn.hubbo.service.auth.SystemMenuService;
import com.alipay.sofa.runtime.api.annotation.SofaReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/sys/menu")
@RequiredArgsConstructor
public class SystemMenuController {

    @SofaReference(interfaceType = SystemMenuService.class)
    private SystemMenuService systemMenuService;

    @GetMapping("/list")
    public ResultVO<?> list() {
        log.info("list data");
        return ResultVO.success(systemMenuService.queryMenuList());
    }

}
