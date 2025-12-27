package cn.hubbo.web.sys

import cn.hubbo.entity.vo.MenuVO
import cn.hubbo.entity.vo.ResultVO
import cn.hubbo.service.auth.SystemMenuService
import lombok.NonNull
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Slf4j
@RestController
@RequestMapping("/sys")
@RequiredArgsConstructor
class SystemMenuApi {

    @NonNull
    private val systemMenuService: SystemMenuService? = null

    companion object {
        @JvmStatic
        val logger: Logger = LoggerFactory.getLogger(SystemMenuApi::class.java)
    }

    @GetMapping("/menu/list")
    fun list(): ResultVO<List<MenuVO>> {
        return ResultVO.success(systemMenuService!!.queryUserMenuList())
    }

    @GetMapping("/routes/list")
    fun routes(): ResultVO<List<MenuVO>> {
        logger.info("routes data")
        // todo 路由处理
        return ResultVO.success(systemMenuService!!.queryUserMenuList())
    }


}
