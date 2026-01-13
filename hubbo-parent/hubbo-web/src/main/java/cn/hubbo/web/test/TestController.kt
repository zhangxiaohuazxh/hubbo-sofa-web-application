package cn.hubbo.web.test

import cn.hubbo.common.constants.LibraryConstants
import cn.hubbo.entity.vo.ResultVO
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Mono
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
    fun localDateTime(): Mono<ResultVO<Any>> {
        logger.info("访问系统时间接口")
        return Mono.just(ResultVO.success(LocalDateTime.now(ZoneId.of(LibraryConstants.DEFAULT_ZONE_ID.value))))
    }

    @PostMapping("/file/upload")
    fun uploadFile(@RequestParam("file") file: MultipartFile, @RequestParam("attr") attr: String): ResultVO<Any> {
        logger.info("接收到的文件名 {} size {}", file.originalFilename, file.size)
        logger.info("attr {}", attr)
        return ResultVO.success()
    }


}
