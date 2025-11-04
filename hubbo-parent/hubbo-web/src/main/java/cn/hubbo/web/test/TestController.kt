package cn.hubbo.web.test

import cn.hubbo.common.constants.LibraryConstants
import cn.hubbo.entity.vo.ResultVO
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.ZoneId

@RestController
@RequestMapping("/test")
class TestController {

    companion object {
        @JvmStatic
        val logger: Logger = LoggerFactory.getLogger(TestController::class.java)
    }

    @GetMapping("/sysdate")
    fun localDateTime(): ResultVO<Any> {
        logger.info("访问系统时间接口")
        return ResultVO.success(LocalDateTime.now(ZoneId.of(LibraryConstants.DEFAULT_ZONE_ID.value)))
    }

}
