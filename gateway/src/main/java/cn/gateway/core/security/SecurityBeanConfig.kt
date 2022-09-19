package cn.gateway.core.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository

/**
 * 提供 spring security 所需要的 bean
 */
@Configuration
class SecurityBeanConfig {
    /**
     * 密码加密方式
     *
     * @return PasswordEncoder
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun serverSecurityContextRepository(): ServerSecurityContextRepository {
        return WebSessionServerSecurityContextRepository()
    }
}