package cn.framework.config.swagger

import cn.framework.common.log.Slf4k.Companion.log
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.SpringfoxWebConfiguration
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import springfox.documentation.swagger2.configuration.Swagger2DocumentationConfiguration

/**
 * swagger 的配置，新增模块与设置在这里
 */
@Configuration
@EnableSwagger2
@Import(SpringfoxWebConfiguration::class)
@ConditionalOnClass(Swagger2DocumentationConfiguration::class)
class SwaggerConfiguration(private val applicationContext: ApplicationContext) {
    @Value("\${spring.application.name:}")
    private lateinit var applicationName: String

    @Value("\${info.moduleName:}")
    private lateinit var moduleName: String

    @Value("\${info.description:}")
    private lateinit var description: String

    private var basePackage: String? = null

    @Bean
    fun groupRestApi(): Docket {
        applicationContext.getBeansWithAnnotation(SpringBootApplication::class.java)
            .forEach { (_, instance) ->
                basePackage = instance.javaClass.getPackage().name
                log.info("swagger scan base package {}", basePackage)
            }
        return Docket(DocumentationType.SWAGGER_2)
            .apiInfo(groupApiInfo())
            .select()
            .apis(RequestHandlerSelectors.basePackage(basePackage))
            .paths(PathSelectors.any())
            .build()
    }

    private fun groupApiInfo(): ApiInfo {
        return ApiInfoBuilder()
            .title("$applicationName:$moduleName")
            .description("<div style='font-size:14px;color:red;'>$description</div>")
            .version("1.0")
            .build()
    }
}