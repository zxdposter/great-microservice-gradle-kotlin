tasks {
    "war"(War::class) {
        enabled = false
    }

    bootJar {
        enabled = false
    }

    jar {
        enabled = true
    }
}

dependencies {
    compileOnly("com.baomidou:mybatis-plus-boot-starter")
    compileOnly("javax.servlet:javax.servlet-api")
    compileOnly("org.springframework:spring-webflux")
    compileOnly(group = "com.github.xiaoymin", name = "knife4j-micro-spring-boot-starter")
    compileOnly("org.springframework:spring-webmvc")
}