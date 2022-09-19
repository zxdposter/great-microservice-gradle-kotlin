package cn.business.web

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession
import org.springframework.web.servlet.config.annotation.EnableWebMvc


@EnableWebMvc
@EnableCaching
@EnableFeignClients
@SpringBootApplication
@EnableRedisHttpSession
class WebApplication : SpringBootServletInitializer() {
    override fun configure(applicationBuilder: SpringApplicationBuilder): SpringApplicationBuilder =
        applicationBuilder.sources(WebApplication::class.java)
}

fun main(args: Array<String>) {
    SpringApplication.run(WebApplication::class.java, *args)
}
