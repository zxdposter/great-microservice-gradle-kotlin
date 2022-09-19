package cn.gateway.core.filter

import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyDecoder
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyEncoder
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ServerCodecConfigurer

/**
 * 读取 request 与 response 的 body.
 * 方法来源于 [ModifyRequestBodyGatewayFilterFactory] 与 [ModifyResponseBodyGatewayFilterFactory]
 */
@Configuration
class BodyReadFilterConfig {
    @Bean
    fun readRequestBodyFilter(): RequestReaderFilter {
        return RequestReaderFilter()
    }

    @Bean
    fun readResponseBodyFilter(
        codecConfigurer: ServerCodecConfigurer,
        bodyDecoders: Set<MessageBodyDecoder>,
        bodyEncoders: Set<MessageBodyEncoder>
    ): ResponseReaderFilter {
        return ResponseReaderFilter(codecConfigurer.readers, bodyDecoders, bodyEncoders)
    }

    companion object {
        const val CACHE_REQUEST_BODY = "cacheRequestBody"
        const val CACHE_RESPONSE_BODY = "cacheResponseBody"
    }
}