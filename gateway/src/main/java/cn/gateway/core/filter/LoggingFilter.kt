package cn.gateway.core.filter

import cn.hutool.core.date.StopWatch
import cn.framework.common.jackson.SystemConstant
import cn.framework.common.log.Slf4k.Companion.log
import cn.framework.common.security.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import org.springframework.web.server.WebSession
import reactor.core.publisher.Mono
import javax.annotation.PostConstruct

/**
 * 打印请求参数及统计执行时长过滤器，将执行时长放在 header 中.
 * 必须继承 WebFilter，如果继承 GatewayFilter 会在 security 之后执行，不能打印一些关于 security 请求的耗时.
 *
 * @author zxd
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 1)
class LoggingFilter : WebFilter {
    /**
     * 排除请求，不打印日志
     */
    @Value("\${gateway.logging-filter.exclude:}")
    private lateinit var exclude: Array<String>
    private lateinit var exchangeMatcher: OrServerWebExchangeMatcher

    @PostConstruct
    fun init() {
        val matchers: MutableList<ServerWebExchangeMatcher> = ArrayList(exclude.size)
        matchers.add(PathPatternParserServerWebExchangeMatcher("/**", HttpMethod.OPTIONS))
        for (pattern in exclude) {
            matchers.add(PathPatternParserServerWebExchangeMatcher(pattern, null))
        }
        exchangeMatcher = OrServerWebExchangeMatcher(matchers)
    }

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        /* 开始计时 */
        val stopWatch = StopWatch()
        stopWatch.start()

        /* 给 Request Header 加上 Request-ID，能够转发给下游系统，在下游系统中的全局异常处理时，打印 Request-ID */
        HttpHeaders.writableHttpHeaders(exchange.request.headers)
            .add(SystemConstant.REQUEST_ID, exchange.request.id)

        exchange.response.beforeCommit {
            /* 过滤不计时请求 */
            exchangeMatcher.matches(exchange)
                .filter { matchResult: ServerWebExchangeMatcher.MatchResult -> !matchResult.isMatch }
                .switchIfEmpty(Mono.empty())
                .flatMap { beforeCommit(exchange, stopWatch) }
        }
        return chain.filter(exchange)
    }

    /**
     * 该方法配合 ServerHttpResponse 的 beforeCommit，在 response 提交之前执行方法，
     * 能够保证日志在最后时刻打印.
     *
     * @param exchange  请求上线文
     * @param stopWatch 耗时记录
     * @return 执行对象
     */
    private fun beforeCommit(exchange: ServerWebExchange, stopWatch: StopWatch): Mono<Void> {
        val request = exchange.request
        val responseHttpHeaders = HttpHeaders.writableHttpHeaders(exchange.response.headers)

        /* 给 Response Header 加上 Request-ID，用于追踪请求 */
        responseHttpHeaders.add(SystemConstant.REQUEST_ID, exchange.request.id)

        return exchange.session.doOnNext { webSession: WebSession? ->
            var userName: String? = null
            webSession?.let {
                val attribute =
                    webSession.getAttribute<Any>(WebSessionServerSecurityContextRepository.DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME)
                if (attribute != null) {
                    val securityContext = attribute as SecurityContextImpl
                    if (securityContext.authentication.principal is User) {
                        userName = (securityContext.authentication.principal as User).username
                    }
                }
            }
            stopWatch.stop()
            val cost = stopWatch.totalTimeMillis
            log.info(
                "{} requestDetail, user {}, method:{}, uri:{}, query:{}, response code:{}, executeTime {} ms",
                exchange.logPrefix,
                userName ?: "is unknown",
                request.method,
                request.uri.toString().replaceFirst("\\?.*$".toRegex(), ""),
                request.queryParams,
                exchange.response.statusCode,
                cost
            )
            /* 给 Response Header 加上 Cost，用于判断耗时 */responseHttpHeaders.add(SystemConstant.COST, cost.toString())
            val requestBody = exchange.getAttribute<String>(BodyReadFilterConfig.CACHE_REQUEST_BODY)
            val responseBody = exchange.getAttribute<String>(BodyReadFilterConfig.CACHE_RESPONSE_BODY)
        }.then()
    }
}