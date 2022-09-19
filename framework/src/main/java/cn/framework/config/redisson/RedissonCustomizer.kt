package cn.framework.config.redisson

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import io.netty.channel.nio.NioEventLoopGroup
import org.redisson.client.codec.Codec
import org.redisson.codec.JsonJacksonCodec
import org.redisson.spring.starter.RedissonAutoConfiguration
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

@Configuration
@AutoConfigureAfter(RedissonAutoConfiguration::class)
@ConditionalOnClass(RedissonAutoConfiguration::class)
class RedissonCustomizer(private val jackson2ObjectMapperBuilder: Jackson2ObjectMapperBuilder) {
    @Bean
    fun redissonCustomizer(): RedissonAutoConfigurationCustomizer {
        val objectMapper = jackson2ObjectMapperBuilder.build<ObjectMapper>()
        val typed: TypeResolverBuilder<*> =
            ObjectMapper.DefaultTypeResolverBuilder(
                ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE,
                LaissezFaireSubTypeValidator.instance
            ).init(JsonTypeInfo.Id.CLASS, null)
                .inclusion(JsonTypeInfo.As.PROPERTY)
                .typeProperty("@class")

        objectMapper.setDefaultTyping(typed)

        return RedissonAutoConfigurationCustomizer { config ->
            val codecIns: Codec = JsonJacksonCodec(objectMapper)
            config.codec = codecIns
            config.threads = 0
            config.eventLoopGroup = NioEventLoopGroup()
        }
    }
}