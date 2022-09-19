package cn.gateway.core.cors

import cn.framework.common.jackson.SystemConstant
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.CorsUtils
import org.springframework.web.server.ServerWebExchange

/**
 * cors webflux 配置
 *
 * @author zxd
 */
@Component
class WebfluxCorsConfig {
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        return CustomCors()
    }

    private class CustomCors : CorsConfigurationSource {
        override fun getCorsConfiguration(exchange: ServerWebExchange): CorsConfiguration {
            val corsConfiguration = CorsConfiguration()
            val request = exchange.request
            if (CorsUtils.isCorsRequest(request)) {
                corsConfiguration.allowCredentials = true
                corsConfiguration.allowedOrigins = listOf(request.headers.origin)
                corsConfiguration.allowedMethods = SystemConstant.ACCESS_CONTROL_ALLOW_METHODS
                corsConfiguration.exposedHeaders = SystemConstant.ACCESS_CONTROL_EXPOSE_HEADERS
                if (request.method == HttpMethod.OPTIONS) {
                    val strings = request.headers[HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS]
                    strings?.forEach {
                        it.split(",".toRegex()).toTypedArray()
                            .map { obj: String -> obj.trim { it <= ' ' } }
                            .forEach { allowedHeader: String -> corsConfiguration.addAllowedHeader(allowedHeader) }
                    }
                }
            }
            return corsConfiguration
        }
    }
}