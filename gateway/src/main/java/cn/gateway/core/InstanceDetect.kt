package cn.gateway.core

import cn.framework.common.log.Slf4k.Companion.log
import com.alibaba.cloud.nacos.NacosServiceManager
import com.alibaba.nacos.api.naming.listener.NamingEvent
import com.alibaba.nacos.api.naming.pojo.Instance
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent
import org.springframework.cloud.gateway.config.GatewayProperties
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

/**
 * 监测 instance 的连接情况
 */
@Service
class InstanceDetect(
    private val nacosServiceManager: NacosServiceManager,
    private val gatewayProperties: GatewayProperties
) {
    @EventListener
    fun onApplicationEvent(event: InstanceRegisteredEvent<*>) {
        val namingService = nacosServiceManager.getNamingService(null)
        gatewayProperties.routes.forEach {
            val serviceInstanceMap = mutableMapOf<String, Instance>()
            namingService.subscribe(it.id) { event ->
                if (event !is NamingEvent) {
                    return@subscribe
                }
                val serviceInstances = HashSet(serviceInstanceMap.keys)
                event.instances.forEach { instance ->
                    if (serviceInstances.contains(instance.instanceId)) {
                        serviceInstances.remove(instance.instanceId)
                    } else {
                        serviceInstanceMap[instance.instanceId] = instance
                        log.info(
                            "instance connected : {}, ip : {}, port : {}",
                            instance.instanceId, instance.ip, instance.port
                        )
                    }
                }
                for (removedInstance in serviceInstances) {
                    val remove = serviceInstanceMap.remove(removedInstance)!!
                    log.info(
                        "instance disconnected : {}, ip : {}, port : {}",
                        removedInstance, remove.ip, remove.port
                    )
                }
            }
        }
    }
}