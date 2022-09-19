package cn.business.foundation.config.session

import cn.framework.common.jackson.SystemConstant
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.session.web.http.HeaderHttpSessionIdResolver
import org.springframework.session.web.http.HttpSessionIdResolver

/**
 * 设置 mvc 和 webflux 保存令牌的方式，使用自定义的 header 保存.
 *
 * @author zxd
 */
@Configuration
@AutoConfigureAfter(SessionAutoConfiguration::class)
@ConditionalOnClass(value = [SessionAutoConfiguration::class, HttpSessionIdResolver::class])
class MvcHttpSessionConfig {
    @Bean
    @ConditionalOnBean(WebMvcAutoConfiguration::class)
    fun httpSessionIdResolver(): HttpSessionIdResolver {
        return HeaderHttpSessionIdResolver(SystemConstant.SESSION_HEADER_NAME)
    }
}