package cn.business.foundation.config.logging

import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration

@Configuration
@AutoConfigureAfter(DelegatingWebMvcConfiguration::class)
@ConditionalOnBean(DelegatingWebMvcConfiguration::class)
class MvcLoggingFilterConfig {
    @Bean
    fun mvcLoggingConfigurer() = MvcLoggingConfigurer()
}