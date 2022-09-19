package cn.framework.config.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import org.redisson.Redisson
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.security.jackson2.SecurityJackson2Modules
import java.util.Optional

/**
 * 使 redis session 在序列化时能够可读.
 *
 * @author zxd
 */
@Configuration
@AutoConfigureAfter(SessionAutoConfiguration::class)
@ConditionalOnBean(SessionAutoConfiguration::class)
@ConditionalOnClass(Redisson::class)
class RedisSessionConfig {
    @Bean
    fun springSessionDefaultRedisSerializer(objectMapperBuilder: Optional<Jackson2ObjectMapperBuilder>): RedisSerializer<Any> {
        val objectMapper: ObjectMapper = objectMapperBuilder
            .map { it.build() as ObjectMapper }
            .orElseGet { ObjectMapper() }
        objectMapper.registerModules(SecurityJackson2Modules.getModules(this.javaClass.classLoader))
        return GenericJackson2JsonRedisSerializer(objectMapper)
    }
}