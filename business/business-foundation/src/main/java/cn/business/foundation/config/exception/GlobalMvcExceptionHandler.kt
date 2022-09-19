package cn.business.foundation.config.exception

import cn.hutool.core.exceptions.ExceptionUtil
import cn.framework.common.http.InnerExp
import cn.framework.common.http.RequestResult
import cn.framework.common.jackson.JacksonObject
import cn.framework.common.jackson.convert
import cn.framework.common.log.Slf4k.Companion.log
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import feign.FeignException.ServiceUnavailable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MultipartException
import org.springframework.web.servlet.NoHandlerFoundException
import java.lang.IllegalArgumentException
import java.time.format.DateTimeParseException
import javax.servlet.http.HttpServletRequest

/**
 * mvc 全局异常处理
 *
 *
 * 会打印出 request id
 *
 * @author zxd
 */
@RestControllerAdvice
class GlobalMvcExceptionHandler {
    /**
     * 处理正常的异常，该异常都是由代码主动抛出
     */
    @ExceptionHandler(InnerExp::class)
    fun handleInnerException(request: HttpServletRequest, exception: InnerExp): ResponseEntity<RequestResult> {
        val requestResult = exception.result
        if (!requestResult.isSuccess) {
            exception.cause?.let {
                log.error("{}, {}, {}", request.requestURI, exception.message, ExceptionUtil.stacktraceToString(it))
            } ?: log.error("{}, {}", request.requestURI, exception.message)
        }
        return ResponseEntity.status(HttpStatus.OK).body(requestResult)
    }

    /**
     * 处理上传文件终端异常，往往是由于客户端终止了上传，有可能有未知的问题出现，因此打印堆栈异常.
     *
     * @param exception 异常对象
     * @return 返回结果
     */
    @ExceptionHandler(MultipartException::class)
    fun handleMultipartException(
        request: HttpServletRequest,
        exception: MultipartException
    ): ResponseEntity<RequestResult> {
        val requestResult = RequestResult.error("请求错误")
            .setReason(exception.message)
        log.error(request.requestURI, exception)
        return ResponseEntity.status(HttpStatus.OK).body(requestResult)
    }

    /**
     * 处理 feign 服务找不到的异常
     */
    @ExceptionHandler(ServiceUnavailable::class)
    fun handleServiceUnavailableException(
        request: HttpServletRequest,
        exception: ServiceUnavailable
    ): ResponseEntity<RequestResult> {
        val requestResult = RequestResult.error("后台服务未启动")
            .setReason(exception.message)
        log.error("{}, {}", request.requestURI, exception.message)
        return ResponseEntity.status(HttpStatus.OK).body(requestResult)
    }

    /**
     * 处理未知异常，该方法被调用意味着出现了没有被处理到的问题，因此需要打印堆栈信息
     */
    @ExceptionHandler(Exception::class)
    fun handleException(request: HttpServletRequest, exception: Exception): ResponseEntity<RequestResult> {
        val requestResult = RequestResult.error("请求内部错误")
            .setReason(exception.message)
        log.error(request.requestURI, exception)
        return ResponseEntity.status(HttpStatus.OK).body(requestResult)
    }

    /**
     * 由于请求参数错误导致，改方法中提供了针对 jackson 序列化错误的详细信息获取
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    protected fun handleHttpMessageNotReadable(
        request: HttpServletRequest,
        exception: HttpMessageNotReadableException
    ): ResponseEntity<RequestResult> {
        // 获取 jackson 序列化错误的详细字段
        fun List<JsonMappingException.Reference>.toPath(): String {
            val sb = StringBuilder()
            this.mapIndexed { index, reference ->
                sb.append(
                    if (index == 0 || reference.fieldName == null) {
                        reference.fieldName ?: "[${reference.index}]"
                    } else {
                        ".${reference.fieldName}"
                    }
                )
            }
            return sb.toString()
        }

        val requestResult = when (val cause = exception.cause) {
            is InvalidFormatException ->
                RequestResult.error("""字段"{}"值错误:"{}"""", cause.path.toPath(), cause.value)

            is MissingKotlinParameterException ->
                RequestResult.error("""缺少必要字段"{}"""", cause.path.toPath())

            is JsonMappingException -> {
                val subCause = cause.cause
                if (subCause is InnerExp) {
                    // 发生于 class init 抛出异常
                    subCause.result
                } else if (subCause is DateTimeParseException) {
                    RequestResult.error("""字段"{}"时间值错误:"{}"""", cause.path.toPath(), subCause.parsedString)
                } else {
                    log.warn("{}", request.requestURI, exception)
                    RequestResult.error("""字段"{}"值错误:"{}"""", cause.path.toPath())
                }
            }

            else -> {
                log.warn("{}", request.requestURI, exception)
                RequestResult.error("请求错误，请检查请求内容")
                    .setReason(exception.message)
            }
        }.apply { log.error(message) }

        return ResponseEntity.status(HttpStatus.OK).body(requestResult)
    }

    @ExceptionHandler(NoHandlerFoundException::class)
    protected fun noHandlerFoundException(
        request: HttpServletRequest,
        exception: NoHandlerFoundException
    ): ResponseEntity<RequestResult> {
        val requestResult = RequestResult.error("请求地址错误")
            .setReason(exception.message)
        log.warn("{}, {}", request.requestURI, exception.message)
        return ResponseEntity.status(HttpStatus.OK).body(requestResult)
    }

    /**
     * 处理校验异常，将校验异常中的提示返回给前台
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    protected fun methodArgumentNotValidException(
        request: HttpServletRequest,
        exception: MethodArgumentNotValidException
    ): ResponseEntity<RequestResult> {
        val requestResult = RequestResult.error()
            .setReason(exception.message)
        requestResult.setMessage(exception.allErrors[0].defaultMessage ?: "参数校验错误")
        log.warn("{}, {}", request.requestURI, requestResult.message)
        return ResponseEntity.status(HttpStatus.OK).body(requestResult)
    }
}