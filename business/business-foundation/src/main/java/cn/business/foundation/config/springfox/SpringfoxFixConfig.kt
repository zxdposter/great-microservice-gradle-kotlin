package cn.business.foundation.config.springfox

import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType
import org.springframework.boot.actuate.endpoint.ExposableEndpoint
import org.springframework.boot.actuate.endpoint.web.EndpointLinksResolver
import org.springframework.boot.actuate.endpoint.web.EndpointMapping
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointsSupplier
import org.springframework.boot.actuate.endpoint.web.annotation.ServletEndpointsSupplier
import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import org.springframework.util.StringUtils

/**
 * 解决spring boot 2.7.0 下，springfox 启动空指针的问题
 */
class SpringfoxFixConfig {
    @Bean
    fun webEndpointServletHandlerMapping(
        webEndpointsSupplier: WebEndpointsSupplier,
        servletEndpointsSupplier: ServletEndpointsSupplier,
        controllerEndpointsSupplier: ControllerEndpointsSupplier,
        endpointMediaTypes: EndpointMediaTypes?,
        corsProperties: CorsEndpointProperties,
        webEndpointProperties: WebEndpointProperties,
        environment: Environment
    ): WebMvcEndpointHandlerMapping {
        val allEndpoints: MutableList<ExposableEndpoint<*>> = ArrayList()
        val webEndpoints = webEndpointsSupplier.endpoints
        allEndpoints.addAll(webEndpoints)
        allEndpoints.addAll(servletEndpointsSupplier.endpoints)
        allEndpoints.addAll(controllerEndpointsSupplier.endpoints)
        val basePath = webEndpointProperties.basePath
        val endpointMapping = EndpointMapping(basePath)
        val shouldRegisterLinksMapping = shouldRegisterLinksMapping(webEndpointProperties, environment, basePath)
        return WebMvcEndpointHandlerMapping(
            endpointMapping,
            webEndpoints,
            endpointMediaTypes,
            corsProperties.toCorsConfiguration(),
            EndpointLinksResolver(allEndpoints, basePath),
            shouldRegisterLinksMapping,
            null
        )
    }

    private fun shouldRegisterLinksMapping(
        webEndpointProperties: WebEndpointProperties,
        environment: Environment,
        basePath: String
    ): Boolean {
        return webEndpointProperties.discovery.isEnabled &&
                (StringUtils.hasText(basePath) ||
                        ManagementPortType.get(environment).equals(ManagementPortType.DIFFERENT))
    }
}