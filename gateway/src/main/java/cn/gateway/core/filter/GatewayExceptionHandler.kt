package cn.gateway.core.filter

import cn.hutool.core.exceptions.ExceptionUtil
import cn.framework.common.http.RequestResult
import cn.framework.common.http.RequestResult.Companion.error
import cn.framework.common.log.Slf4k.Companion.log
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * gateway 全局异常处理.
 *
 * @author zxd
 */
@Order(-1)
@ControllerAdvice
class GatewayExceptionHandler : ErrorWebExceptionHandler {
    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        val request = exchange.request

        /*
        正常情况下，不会出现该情况
         */
        if (request.path.toString().startsWith("/favicon.ico")) {
            return Mono.empty()
        }

        val response = exchange.response
        val httpHeaders = HttpHeaders.writableHttpHeaders(response.headers)
        httpHeaders.remove(HttpHeaders.CONTENT_TYPE)
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        var requestResult = error().setMessage(ex.message)
        response.statusCode = HttpStatus.OK
        if (ex is AuthenticationException) {
            dealAuthenticationException(requestResult, exchange, ex)
        } else if (ex is ResponseStatusException) {
            dealResponseStatusException(requestResult, exchange, ex)
        } else if (ex is cn.framework.common.http.InnerExp) {
            requestResult = ex.result
            if (ex.cause != null) {
                log.error(
                    "{} {}, {}",
                    exchange.logPrefix,
                    ex.message,
                    ExceptionUtil.stacktraceToString(ex.cause)
                )
            } else {
                log.error("{} {}", exchange.logPrefix, ex.message)
            }
        } else {
            log.error(String.format("%s url %s", exchange.logPrefix, request.uri), ex)
            requestResult.setMessage("内部错误，请联系管理员")
        }
        requestResult.setReason("${request.uri} : ${ex.message}")
        val buffer = response.bufferFactory().wrap(requestResult.toJsonBytes())
        return response.writeWith(Flux.just(buffer))
    }

    /**
     * 处理登录验证错误.
     */
    private fun dealAuthenticationException(
        requestResult: RequestResult,
        exchange: ServerWebExchange,
        authenticationException: AuthenticationException
    ) {
        var account = ""
        exchange.formData.toFuture().get()?.let {
            it["account"]?.get(0)?.let { v ->
                account = v
            }
        }
        val request = exchange.request
        when (authenticationException) {
            is AuthenticationCredentialsNotFoundException -> {
                requestResult.setMessage("用户未登录")
                log.warn(
                    "{} Unauthorized operation, url {}, remote address {}", exchange.logPrefix,
                    request.uri,
                    request.remoteAddress
                )
            }
            is BadCredentialsException -> {
                requestResult.setMessage("用户名或密码错误")
                log.error(
                    "{} login error remote address {}, account {}",
                    exchange.logPrefix,
                    request.remoteAddress,
                    account
                )
            }
            is LockedException -> {
                requestResult.setMessage("账号登录错误次数过多，已锁定，请联系管理员解锁")
                log.error(
                    "${exchange.logPrefix} authentication error remote address ${request.remoteAddress}, info {}, account {}",
                    authenticationException.message, account
                )
            }
            else -> {
                requestResult.setMessage("用户验证错误")
                log.error(
                    exchange.logPrefix + " authentication error remote address " + request.remoteAddress,
                    authenticationException
                )
            }
        }
    }

    /**
     * 处理请求状态错误.
     */
    private fun dealResponseStatusException(
        requestResult: RequestResult,
        exchange: ServerWebExchange,
        statusException: ResponseStatusException
    ) {
        val request = exchange.request
        when (statusException.status) {
            HttpStatus.NOT_FOUND -> requestResult.setMessage("请求地址不存在")
            HttpStatus.GATEWAY_TIMEOUT -> requestResult.setMessage("请求超时")
            HttpStatus.SERVICE_UNAVAILABLE -> requestResult.setMessage("服务暂时不可用")
            else -> requestResult.setMessage("请求状态异常 : {}", statusException.status)
        }
        log.warn(
            "{} {}, uri {}, remote address {}, request method {}, reason {}",
            exchange.logPrefix, statusException.status,
            request.uri,
            request.remoteAddress,
            request.method,
            statusException.message
        )
    }
}