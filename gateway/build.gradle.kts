tasks {
    bootJar {
        enabled = true
    }

    jar {
        enabled = true
    }

    "war"(War::class) {
        enabled = true
    }
}

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    implementation("com.github.xiaoymin:knife4j-spring-boot-starter")
    compileOnly(group = "io.netty", name = "netty-resolver-dns-native-macos", classifier = "osx-aarch_64")
}