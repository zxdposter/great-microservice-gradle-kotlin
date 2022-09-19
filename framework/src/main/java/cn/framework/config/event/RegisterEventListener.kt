package cn.framework.config.event

import cn.framework.common.log.Slf4k.Companion.log
import com.alibaba.cloud.nacos.NacosDiscoveryProperties
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener

/**
 * 成功注册到 nacos 的事件打印
 */
@Configuration
class RegisterEventListener {
    @EventListener
    fun listener(event: InstanceRegisteredEvent<NacosDiscoveryProperties>) {
        val properties = event.config
        log.info("instance registered success, info {}", properties)
    }
}