package cn.gateway.core.filter

import cn.framework.common.log.Slf4k.Companion.log
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory
import org.springframework.cloud.gateway.support.BodyInserterContext
import org.springframework.cloud.gateway.support.GatewayToStringStyler
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.codec.HttpMessageReader
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpRequestDecorator
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.HandlerStrategies
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.function.Function

/**
 * 读取 request 的 body.
 * 方法来源于 [ModifyRequestBodyGatewayFilterFactory]
 */
class RequestReaderFilter : ModifyRequestBodyGatewayFilterFactory() {
    companion object {
        const val CACHE_REQUEST_BODY = "cacheRequestBody"
    }

    private val messageReaders: List<HttpMessageReader<*>> = HandlerStrategies.withDefaults().messageReaders()

    override fun apply(config: Config): GatewayFilter {
        return object : GatewayFilter {
            override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
                val contentType = exchange.request.headers.contentType
                val contentLength = exchange.request.headers.contentLength
                if (MediaType.MULTIPART_FORM_DATA.equalsTypeAndSubtype(contentType)) {
                    return chain.filter(exchange)
                }
                if (contentLength >= 262144) {
                    log.warn(
                        "{} request content length {} too large, content type {}",
                        exchange.logPrefix,
                        contentLength,
                        contentType
                    )
                    return chain.filter(exchange)
                }
                val serverRequest = ServerRequest.create(exchange, messageReaders)
                val modifiedBody = serverRequest.bodyToMono(String::class.java)
                    .flatMap { originalBody: String? ->
                        originalBody?.let {
                            exchange.attributes[CACHE_REQUEST_BODY] = it
                            Mono.just(it)
                        } ?: Mono.empty()
                    }
                val bodyInserter = BodyInserters.fromPublisher(modifiedBody, String::class.java)
                val headers = HttpHeaders()
                headers.putAll(exchange.request.headers)
                headers.remove(HttpHeaders.CONTENT_LENGTH)
                if (config.contentType != null) {
                    headers[HttpHeaders.CONTENT_TYPE] = config.contentType
                }
                val outputMessage = CachedBodyOutputMessage(exchange, headers)
                return bodyInserter.insert(outputMessage, BodyInserterContext())
                    .then(Mono.defer {
                        val decorator: ServerHttpRequest = decorate(exchange, headers, outputMessage)
                        chain.filter(exchange.mutate().request(decorator).build())
                    }).onErrorResume(Function { throwable: Throwable ->
                        release(exchange, outputMessage, throwable)
                    } as Function<Throwable, Mono<Void>>)
            }

            override fun toString(): String {
                return GatewayToStringStyler.filterToStringCreator(this)
                    .append("Content type", config.contentType)
                    .append("In class", config.inClass)
                    .append("Out class", config.outClass).toString()
            }
        }
    }

    fun decorate(
        exchange: ServerWebExchange, headers: HttpHeaders,
        outputMessage: CachedBodyOutputMessage
    ): ServerHttpRequestDecorator {
        return object : ServerHttpRequestDecorator(exchange.request) {
            override fun getHeaders(): HttpHeaders {
                val contentLength = headers.contentLength
                val httpHeaders = HttpHeaders()
                httpHeaders.putAll(headers)
                if (contentLength > 0) {
                    httpHeaders.contentLength = contentLength
                } else {
                    httpHeaders[HttpHeaders.TRANSFER_ENCODING] = "chunked"
                }
                return httpHeaders
            }

            override fun getBody(): Flux<DataBuffer> {
                return outputMessage.body
            }
        }
    }
}