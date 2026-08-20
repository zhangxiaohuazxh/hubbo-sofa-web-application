package cn.hubbo.handler

import cn.hubbo.entity.vo.ResultVO
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * 全局异常处理器：统一将业务/系统异常包装为 [ResultVO]，
 * 避免向前端暴露堆栈信息，并保证所有接口响应格式一致。
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger: Logger by lazy { LoggerFactory.getLogger(GlobalExceptionHandler::class.java) }

    @ExceptionHandler(UnsupportedOperationException::class)
    fun handleNotImplemented(e: UnsupportedOperationException): ResponseEntity<ResultVO<*>> {
        logger.warn("未实现的操作被调用: {}", e.message)
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(ResultVO<Any>(501, e.message ?: "Not implemented"))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(e: IllegalArgumentException): ResponseEntity<ResultVO<*>> {
        logger.warn("非法参数: {}", e.message)
        return ResponseEntity.badRequest().body(ResultVO<Any>(400, e.message ?: "Bad request"))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(e: Exception): ResponseEntity<ResultVO<*>> {
        logger.error("未处理的异常", e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ResultVO<Any>(500, "Internal server error"))
    }

}