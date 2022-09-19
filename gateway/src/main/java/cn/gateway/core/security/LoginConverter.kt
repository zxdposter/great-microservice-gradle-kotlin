package cn.gateway.core.security

import cn.framework.common.http.InnerExp.Companion.ifThr
import cn.framework.common.http.RequestResult.Companion.error
import cn.framework.common.jackson.SystemConstant
import cn.framework.common.log.Slf4k.Companion.log
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebSession
import reactor.core.publisher.Mono

/**
 * 自定义登录验证
 *
 *
 * 负责校验验证码是否正确，与返回密码的解密和返回
 *
 * @author zxd
 */
@Component
class LoginConverter : ServerAuthenticationConverter {
    /**
     * 是否开启验证码登录
     */
    @Value("\${gateway.login.verify.enable:false}")
    private var verifyEnable = false

    private fun createAuthentication(data: MultiValueMap<String, String>, logPrefix: String): Authentication {
        val account = data.getFirst(USERNAME_PARAMETER)
        val password = data.getFirst(PASSWORD_PARAMETER)
        ifThr(account.isNullOrEmpty(), "用户名不能为空")
        ifThr(password.isNullOrEmpty(), "密码不能为空")
        log.info("{} login user {} password {}", logPrefix, account, password)
        return UsernamePasswordAuthenticationToken(account, password)
    }

    override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
        val logPrefix = exchange.logPrefix
        return if (verifyEnable) {
            Mono.zip(
                exchange.formData,
                exchange.session
            ) { formData: MultiValueMap<String, String>, webSession: WebSession ->
                ifThr(!formData.containsKey(CODE), "非法请求，未携带验证码")
                ifThr(!webSession.attributes.containsKey(CODE), error("验证码已失效"))
                ifThr(formData.getFirst(CODE) != webSession.getAttribute(CODE), "验证码错误")
                createAuthentication(formData, logPrefix)
            }
        } else {
            exchange.formData.map { createAuthentication(it, logPrefix) }
                .doOnError { exchange.response.statusCode = HttpStatus.FORBIDDEN }
        }
    }

    companion object {
        private const val USERNAME_PARAMETER = "account"
        private const val PASSWORD_PARAMETER = "password"
        private const val CODE = SystemConstant.VERIFY_CODE
    }
}