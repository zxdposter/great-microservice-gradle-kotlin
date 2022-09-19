package cn.gateway.core.security

import cn.hutool.captcha.CaptchaUtil
import cn.framework.common.http.RequestResult
import cn.framework.common.http.RequestResult.Companion.success
import cn.framework.common.jackson.JacksonObject
import cn.framework.common.jackson.SystemConstant
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.time.Duration

/**
 * 获取 base64 验证码图片
 *
 *
 * 当**gateway.login.verify.enable** 为 **true** 时生效
 *
 * @author zxd
 */
@RestController
class VerifyCodeController {
    /**
     * 是否开启验证码登录
     */
    @Value("\${gateway.login.verify.enable:false}")
    private var verifyEnable = false

    /**
     * 验证码失效时间，默认 60 秒
     */
    @Value("\${gateway.login.verify.timeout:60}")
    private var verifyTimeout: Long = 0

    /**
     * 验证码图片宽度
     */
    @Value("\${gateway.login.verify.image.width:106}")
    private var verifyImageWidth = 0

    /**
     * 验证码图片高度
     */
    @Value("\${gateway.login.verify.image.height:35}")
    private var verifyImageHeight = 0

    /**
     * 验证字符数
     */
    @Value("\${gateway.login.verify.length:4}")
    private var verifyCodeLength = 0

    /**
     * 验证图片复杂度
     */
    @Value("\${gateway.login.verify.image.complex:100}")
    private var verifyImageComplex = 0

    @GetMapping("verifyCode")
    fun verifyCode(webExchange: ServerWebExchange): Mono<RequestResult> {
        return webExchange.session.map {
            val jacksonObject = JacksonObject().put("verifyEnable", verifyEnable)
            if (verifyEnable) {
                val lineCaptcha = CaptchaUtil.createLineCaptcha(
                    verifyImageWidth,
                    verifyImageHeight,
                    verifyCodeLength,
                    verifyImageComplex
                )
                lineCaptcha.createCode()
                it.maxIdleTime = Duration.ofSeconds(verifyTimeout)
                it.attributes[SystemConstant.VERIFY_CODE] = lineCaptcha.code
                return@map success().setValue(jacksonObject.put("image", lineCaptcha.imageBase64Data))
            } else {
                return@map success().setValue(jacksonObject)
            }
        }
    }
}