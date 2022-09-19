package cn.business.foundation.config.security


import cn.framework.common.log.Slf4k.Companion.log
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration

/**
 * spring boot security 配置，作为客户端不可登录，只能通过 web session 访问。
 * @author zxd
 */
@Configuration
@AutoConfigureAfter(HttpSecurity::class)
@ConditionalOnClass(SecurityFilterChain::class)
@ConditionalOnBean(DelegatingWebMvcConfiguration::class)
class MvcSecurityHttpConfig(val context: ApplicationContext) {
    @Bean
    fun webSecurityConfigurerAdapter(http: HttpSecurity): SecurityFilterChain {
        http.httpBasic().disable()
            .csrf().disable()
            .formLogin().disable()
            .logout().disable()
            .authorizeRequests()
            .anyRequest().permitAll()
        log.info("config security complete")
        return http.build()
    }
}
