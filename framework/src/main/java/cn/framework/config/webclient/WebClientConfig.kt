package cn.framework.config.webclient

import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration
import org.springframework.boot.autoconfigure.web.reactive.function.client.ClientHttpConnectorAutoConfiguration
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

/**
 * 具有负载能力的 webclient
 */
@Configuration
@ConditionalOnClass(WebClient::class)
@AutoConfigureAfter(
    CodecsAutoConfiguration::class, ClientHttpConnectorAutoConfiguration::class
)
class WebClientConfig {
    @Bean
    @LoadBalanced
    fun loadBalancedWebClientBuilder(): WebClient.Builder = WebClient.builder()
}