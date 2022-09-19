package cn.gateway.core.swagger

import org.springframework.cloud.gateway.config.GatewayProperties
import org.springframework.cloud.gateway.route.Route
import org.springframework.cloud.gateway.route.RouteDefinition
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.support.NameUtils
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import springfox.documentation.swagger.web.SwaggerResource
import springfox.documentation.swagger.web.SwaggerResourcesProvider

/**
 * knife 的 swagger 静态资源配置
 *
 * @author zxd
 */
@Primary
@Component
class SwaggerResourceConfig(
    private val routeLocator: RouteLocator,
    private val gatewayProperties: GatewayProperties
) : SwaggerResourcesProvider {

    override fun get(): List<SwaggerResource> {
        val resources = ArrayList<SwaggerResource>()
        val routes = ArrayList<String>()
        routeLocator.routes.subscribe { route: Route -> routes.add(route.id) }
        gatewayProperties.routes
            .filter { routes.contains(it.id) }
            .forEach { route: RouteDefinition ->
                route.predicates.filter { "Path".equals(it.name, ignoreCase = true) }
                    .forEach {
                        resources.add(
                            swaggerResource(
                                route.id, it.args[NameUtils.GENERATED_NAME_PREFIX + "0"]!!.replace(
                                    "**", "v2/api-docs"
                                )
                            )
                        )
                    }
            }
        return resources
    }

    private fun swaggerResource(name: String, location: String): SwaggerResource {
        val swaggerResource = SwaggerResource()
        swaggerResource.name = name
        swaggerResource.location = location
        swaggerResource.swaggerVersion = "2.0"
        return swaggerResource
    }
}