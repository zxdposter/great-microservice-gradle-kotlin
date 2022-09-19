package cn.gateway.core.swagger

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import springfox.documentation.swagger.web.SecurityConfiguration
import springfox.documentation.swagger.web.SecurityConfigurationBuilder
import springfox.documentation.swagger.web.SwaggerResourcesProvider
import springfox.documentation.swagger.web.UiConfiguration
import springfox.documentation.swagger.web.UiConfigurationBuilder

/**
 * knife 的 swagger 配置
 *
 * @author zxd
 */
@RestController
class SwaggerHandler constructor(private val swaggerResources: SwaggerResourcesProvider) {
    @Autowired(required = false)
    private var securityConfiguration: SecurityConfiguration? = null

    @Autowired(required = false)
    private var uiConfiguration: UiConfiguration? = null

    @GetMapping("/swagger-resources/configuration/security")
    fun securityConfiguration(): Mono<ResponseEntity<SecurityConfiguration>> {
        return Mono.just(
            ResponseEntity(
                securityConfiguration ?: SecurityConfigurationBuilder.builder().build(),
                HttpStatus.OK
            )
        )
    }

    @GetMapping("/swagger-resources/configuration/ui")
    fun uiConfiguration(): Mono<ResponseEntity<UiConfiguration>> {
        return Mono.just(
            ResponseEntity(
                uiConfiguration ?: UiConfigurationBuilder.builder().build(), HttpStatus.OK
            )
        )
    }

    @GetMapping("/swagger-resources")
    fun swaggerResources(): Mono<ResponseEntity<Any>> {
        return Mono.just(ResponseEntity(swaggerResources.get(), HttpStatus.OK))
    }
}