package cn.gateway.core.security

import cn.framework.common.http.RequestResult.Companion.success
import cn.framework.common.jackson.SystemConstant
import cn.framework.common.log.Slf4k.Companion.log
import org.apache.http.HttpHeaders
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authorization.AuthorizationContext
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher
import org.springframework.session.data.redis.ReactiveRedisSessionRepository
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * spring boot security 配置，定义登录方式，session 保存方式，密码加密方式.
 *
 *
 * 网关使用该模块作为登录模块服务端，其它模块作为客户端不可登录。
 * 只能使用 web session 来访问客户端模块。
 *
 * @author zxd
 */
@Configuration
@EnableWebFluxSecurity
class WebfluxSecurityConfig(
    private val authenticationManager: AuthenticationManager,
    private val userDetailsService: UserDetailsServiceImpl
) {
    @Value("\${gateway.security.exclude:}")
    private lateinit var exclude: Array<String>

    /**
     * session 最大有效时间
     */
    @Value("\${gateway.session.max-interval-seconds:1800}")
    private var sessionMaxIntervalSeconds: Int = 0

    /**
     * 是否开启登录
     */
    @Value("\${gateway.security.enable:false}")
    private var securityEnable = false

    @Bean
    fun webFluxSecurityFilterChain(
        sessionRepository: ReactiveRedisSessionRepository,
        http: ServerHttpSecurity,
        discoveryClient: DiscoveryClient,
        loginConverter: LoginConverter
    ): SecurityWebFilterChain {
        sessionRepository.setDefaultMaxInactiveInterval(sessionMaxIntervalSeconds)
        return if (!securityEnable) {
            http.authorizeExchange().anyExchange().permitAll().and().csrf().disable().build()
        } else setLogin(http)
            .setLogout(http)
            .setPermission(http, discoveryClient, loginConverter)
    }

    private fun setLogin(http: ServerHttpSecurity): WebfluxSecurityConfig {
        val formLoginSpec = http.formLogin()
        formLoginSpec.loginPage("/login")
            .authenticationManager(authenticationManager)
            .authenticationEntryPoint { _, exception: AuthenticationException -> throw exception }
            .authenticationFailureHandler { exchange, exception: AuthenticationException ->
                if (exception is BadCredentialsException) {
                    exchange.exchange.formData.block()?.let { map ->
                        map["account"]?.get(0)?.let {
                            userDetailsService.onLoginFailure(it)
                        }
                    }
                }
                throw exception
            }
            .authenticationSuccessHandler { webFilterExchange: WebFilterExchange, _ ->
                val bytes = success().setMessage("登录成功").toJsonBytes()
                val response = webFilterExchange.exchange.response
                response.headers.remove(HttpHeaders.CONTENT_TYPE)
                response.headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                val wrap = response.bufferFactory().wrap(bytes)

                webFilterExchange.exchange.formData.block()?.let { map ->
                    map["account"]?.get(0)?.let {
                        userDetailsService.onLoginSuccess(it)
                    }
                }
                webFilterExchange.exchange.session
                    .doOnNext { webSession: WebSession -> webSession.attributes.remove(SystemConstant.VERIFY_CODE) }
                    .then(response.writeWith(Flux.just(wrap)))
            }
        return this
    }

    private fun setLogout(http: ServerHttpSecurity): WebfluxSecurityConfig {
        http.logout()
            .requiresLogout(PathPatternParserServerWebExchangeMatcher("/logout", HttpMethod.POST))
            .logoutSuccessHandler { exchange: WebFilterExchange, _ ->
                val bytes = success().setMessage("注销成功").toJsonBytes()
                val response = exchange.exchange.response
                response.headers.remove(HttpHeaders.CONTENT_TYPE)
                response.headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                val wrap = response.bufferFactory().wrap(bytes)
                exchange.exchange.session.doOnNext { it.invalidate() }
                    .then(response.writeWith(Flux.just(wrap)))
            }
        return this
    }

    private fun setPermission(
        http: ServerHttpSecurity,
        discoveryClient: DiscoveryClient,
        loginConverter: LoginConverter
    ): SecurityWebFilterChain {
        val build = http
            .authorizeExchange()
            .pathMatchers(*exclude).permitAll()
            .matchers(PathPatternParserServerWebExchangeMatcher("/**", HttpMethod.OPTIONS)).permitAll()
            .pathMatchers("/actuator/**")
            .access { _, context: AuthorizationContext ->
                val request = context.exchange.request
                if (request.remoteAddress == null) {
                    return@access Mono.just(AuthorizationDecision(false))
                }
                val host = request.remoteAddress!!.hostString
                val match = discoveryClient.services
                    .map { discoveryClient.getInstances(it) }
                    .any { serviceInstances -> serviceInstances.any { host == it.host } }
                if (match) {
                    return@access Mono.just(AuthorizationDecision(true))
                }
                log.warn("unknown request address from {}, uri {}", host, request.uri)
                Mono.just(AuthorizationDecision(false))
            }
            .anyExchange().authenticated()
            .and()
            .csrf().disable()
            .httpBasic().disable()
            .build()
        build.webFilters.collectList().subscribe { webFilters: List<WebFilter?> ->
            for (filter in webFilters) {
                if (filter is AuthenticationWebFilter) {
                    filter.setServerAuthenticationConverter(loginConverter)
                }
            }
        }
        return build
    }
}