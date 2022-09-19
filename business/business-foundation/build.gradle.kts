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
//    implementation(group = "org.springframework.data", name = "spring-data-commons")
//    compileOnly(group = "co.elastic.clients", name = "elasticsearch-java")
    compileOnly(group = "com.github.xiaoymin", name = "knife4j-micro-spring-boot-starter")
}