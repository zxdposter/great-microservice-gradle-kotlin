package cn.gateway.core.filter

import org.reactivestreams.Publisher
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyDecoder
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyEncoder
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.MediaType
import org.springframework.http.codec.HttpMessageReader
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * 读取 response 的 body.
 * 方法来源于 [ModifyResponseBodyGatewayFilterFactory]
 */
class ResponseReaderFilter(
    messageReaders: List<HttpMessageReader<*>>,
    messageBodyDecoders: Set<MessageBodyDecoder>,
    messageBodyEncoders: Set<MessageBodyEncoder>
) : ModifyResponseBodyGatewayFilterFactory(messageReaders, messageBodyDecoders, messageBodyEncoders) {
    private val function = RewriteFunction { exchange: ServerWebExchange, body: String? ->
        body?.let {
            exchange.attributes[CACHE_RESPONSE_BODY] = it
            Mono.just(it)
        } ?: Mono.empty()
    }

    override fun apply(config: Config): GatewayFilter {
        config.setRewriteFunction(String::class.java, String::class.java, function)
        val gatewayFilter: ModifyResponseGatewayFilter = object : ModifyResponseGatewayFilter(config) {
            override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
                val modifiedServerHttpResponse: ModifiedServerHttpResponse =
                    object : ModifiedServerHttpResponse(exchange, config) {
                        override fun writeWith(body: Publisher<out DataBuffer?>): Mono<Void> {
                            return if (cache(delegate)) {
                                super.writeWith(body)
                            } else {
                                delegate.writeWith(body)
                            }
                        }
                    }
                return chain.filter(exchange.mutate().response(modifiedServerHttpResponse).build())
            }
        }
        gatewayFilter.setFactory(this)
        return gatewayFilter
    }

    companion object {
        const val CACHE_RESPONSE_BODY = "cacheResponseBody"

        /**
         * 预留用于判断是否缓存.
         *
         * @param serverHttpResponse 响应
         * @return 是否缓存
         */
        private fun cache(serverHttpResponse: ServerHttpResponse): Boolean {
            return MediaType.APPLICATION_JSON == serverHttpResponse.headers.contentType
        }
    }
}