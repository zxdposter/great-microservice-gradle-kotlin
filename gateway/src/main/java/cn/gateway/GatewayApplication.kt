package cn.gateway

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession

/**
 * spring cloud gateway
 *
 *
 * 负责处理转发与鉴权，也是 knife swagger 的中心节点
 *
 * @author zxd
 */
@EnableRedisWebSession
@SpringBootApplication
class GatewayApplication {
}

fun main(args: Array<String>) {
    SpringApplication.run(GatewayApplication::class.java, *args)
}