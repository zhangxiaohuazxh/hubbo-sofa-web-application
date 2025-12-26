package cn.hubbo.web.test

import cn.hubbo.common.constants.LibraryConstants
import cn.hubbo.entity.vo.ResultVO
import cn.hubbo.service.auth.CommonService
import com.alipay.sofa.runtime.api.annotation.SofaReference
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.time.ZoneId

@RestController
@RequestMapping("/test")
class TestController {

    companion object {
        @JvmStatic
        val logger: Logger = LoggerFactory.getLogger(TestController::class.java)
    }

    @SofaReference
    val commonService: CommonService? = null

    @GetMapping("/sysdate")
    fun localDateTime(): ResultVO<Any> {
        logger.info("访问系统时间接口")
        return ResultVO.success(LocalDateTime.now(ZoneId.of(LibraryConstants.DEFAULT_ZONE_ID.value)))
    }

    @PostMapping("/file/upload")
    fun uploadFile(@RequestParam("file") file: MultipartFile, @RequestParam("attr") attr: String): ResultVO<Any> {
        logger.info("接收到的文件名 {} size {}", file.originalFilename, file.size)
        logger.info("attr {}", attr)
        return ResultVO.success()
    }

    @PostMapping("/request/body")
    fun requestBody(@RequestBody body: Map<String, Any>): ResultVO<Any> {
        logger.info("接收到的请求体参数 {}", body)
        return ResultVO.success()
    }

    @GetMapping("/db/version")
    fun dbVersion(): ResultVO<Any> {
        logger.info("访问数据库版本接口")
        return ResultVO.success(commonService!!.dbVersion())
    }


}
