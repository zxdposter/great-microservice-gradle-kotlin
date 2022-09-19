package cn.business.foundation.config.exception

import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration

/**
 * mvc 的全局异常拦截.
 *
 * @author zxd
 */
@Configuration
@AutoConfigureAfter(DelegatingWebMvcConfiguration::class)
@ConditionalOnBean(DelegatingWebMvcConfiguration::class)
class MvcExceptionHandlerConfig {
    @Bean
    fun exceptionHandler(): GlobalMvcExceptionHandler {
        return GlobalMvcExceptionHandler()
    }
}