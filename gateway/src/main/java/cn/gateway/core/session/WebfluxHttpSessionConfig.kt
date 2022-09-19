package cn.gateway.core.session

import cn.framework.common.jackson.SystemConstant
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.server.session.HeaderWebSessionIdResolver
import org.springframework.web.server.session.WebSessionIdResolver

/**
 * 设置 mvc 和 webflux 保存令牌的方式，使用自定义的 header 保存.
 *
 * @author zxd
 */
@Configuration
@AutoConfigureAfter(SessionAutoConfiguration::class)
@ConditionalOnClass(SessionAutoConfiguration::class)
class WebfluxHttpSessionConfig {
    @Bean
    fun webSessionIdResolver(): WebSessionIdResolver {
        val resolver = HeaderWebSessionIdResolver()
        resolver.headerName = SystemConstant.SESSION_HEADER_NAME
        return resolver
    }
}