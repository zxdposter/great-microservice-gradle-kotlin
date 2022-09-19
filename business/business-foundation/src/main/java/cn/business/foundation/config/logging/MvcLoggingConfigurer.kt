package cn.business.foundation.config.logging

import cn.hutool.core.text.CharSequenceUtil
import cn.framework.common.jackson.SystemConstant
import org.slf4j.MDC
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class MvcLoggingConfigurer : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(LoggingHandlerInterceptor())
    }

    internal class LoggingHandlerInterceptor : HandlerInterceptor {
        override fun preHandle(
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse,
            o: Any
        ): Boolean {
            val requestId = httpServletRequest.getHeader(SystemConstant.REQUEST_ID)
            if (CharSequenceUtil.isNotBlank(requestId)) {
                MDC.put(SystemConstant.LOGGING_PATTERN_REQUEST_ID, "[$requestId]")
            }
            return true
        }
    }
}