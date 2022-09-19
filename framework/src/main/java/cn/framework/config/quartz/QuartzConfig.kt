package cn.framework.config.quartz

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@AutoConfigureAfter(RemoteQuartzJob::class)
@ConditionalOnClass(RemoteQuartzJob::class)
class QuartzConfig {
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnAvailableEndpoint
    fun quartzClientJobEndPoint(): QuartzClientJobEndPoint {
        return QuartzClientJobEndPoint()
    }
}