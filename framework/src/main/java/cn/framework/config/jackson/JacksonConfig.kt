package cn.framework.config.jackson

import cn.framework.common.jackson.Jackson.Companion.setObjectMapper
import cn.framework.common.jackson.MultipleLocalDateTimeSerializer
import cn.framework.common.security.User
import cn.framework.common.security.UserGrantedRoleAuthority
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import reactor.util.function.Tuple2
import reactor.util.function.Tuple3
import reactor.util.function.Tuple4
import reactor.util.function.Tuple5
import reactor.util.function.Tuple6
import reactor.util.function.Tuple7
import reactor.util.function.Tuple8
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * webflux 与 mvc 全局 jackson objectMapper 配置.
 *
 *
 * 同时需要排除 monitor，否则 monitor 无法使用
 *
 *
 * 单独 spring.jackson.date-format 似乎没有作用，于是拿来做一个配置.
 *
 * 除了配置之外，使用了 [MultipleLocalDateTimeSerializer]，来支持多种格式的日期输入
 * 目前支持的有 ("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
 *
 * @author zxd
 */
@Configuration
class JacksonConfig {
    @Value("\${spring.jackson.date-format:$DEFAULT_DATE_FORMATTER}")
    private lateinit var dateFormatter: String

    @Bean
    @Primary
    fun jackson2ObjectMapperBuilder(): Jackson2ObjectMapperBuilder {
        val multipleLocalDateTimeSerializer = MultipleLocalDateTimeSerializer(
            dateFormatter,
            arrayOf("yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        )
        val jackson2ObjectMapperBuilder =
            objectMapperBuilder
                .deserializerByType(LocalDateTime::class.java, multipleLocalDateTimeSerializer)
                .simpleDateFormat(dateFormatter)
        setObjectMapper(jackson2ObjectMapperBuilder.build())
        return jackson2ObjectMapperBuilder
    }

    companion object {
        private const val DEFAULT_DATE_FORMATTER = "yyyy-MM-dd HH:mm:ss"

        val objectMapperBuilder = builder()

        fun build(): ObjectMapper {
            return objectMapperBuilder.build()
        }

        private fun builder(): Jackson2ObjectMapperBuilder {
            val allModules: MutableList<Module> = ArrayList()
            val module = JavaTimeModule()
            val localDateTimeSerializer = LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMATTER))
            module.addDeserializer(
                LocalDateTime::class.java,
                MultipleLocalDateTimeSerializer(
                    "yyyy-MM-dd HH:mm:ss",
                    arrayOf("yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                )
            )
            module.addSerializer(LocalDateTime::class.java, localDateTimeSerializer)
            val simpleModule = SimpleModule()
            simpleModule.addDeserializer(Tuple2::class.java, TupleDeserializer())
            simpleModule.addDeserializer(Tuple3::class.java, TupleDeserializer())
            simpleModule.addDeserializer(Tuple4::class.java, TupleDeserializer())
            simpleModule.addDeserializer(Tuple5::class.java, TupleDeserializer())
            simpleModule.addDeserializer(Tuple6::class.java, TupleDeserializer())
            simpleModule.addDeserializer(Tuple7::class.java, TupleDeserializer())
            simpleModule.addDeserializer(Tuple8::class.java, TupleDeserializer())
            val userModule: SimpleModule = object : SimpleModule() {
                override fun setupModule(context: SetupContext) {
                    /* 只注册部分安全类的反序列化 */
                    context.setMixInAnnotations(User::class.java, UserMixin::class.java)
                    context.setMixInAnnotations(
                        UserGrantedRoleAuthority::class.java,
                        UserGrantedRoleAuthorityMixin::class.java
                    )
                }
            }
            allModules.add(module)
            allModules.add(simpleModule)
            allModules.add(userModule)
            allModules.add(kotlinModule())
            return Jackson2ObjectMapperBuilder()
                .simpleDateFormat(DEFAULT_DATE_FORMATTER)
                .modules(allModules)
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .featuresToEnable(MapperFeature.USE_BASE_TYPE_AS_DEFAULT_IMPL)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .findModulesViaServiceLoader(true)
        }
    }
}