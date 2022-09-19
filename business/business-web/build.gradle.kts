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
    implementation(group = "com.github.xiaoymin", name = "knife4j-micro-spring-boot-starter")
    implementation(group = "com.baomidou", name = "mybatis-plus-boot-starter")
    implementation(group = "com.alibaba", name = "easyexcel")

    runtimeOnly(group = "mysql", name = "mysql-connector-java")

    compileOnly(group = "io.netty", name = "netty-resolver-dns-native-macos", classifier = "osx-aarch_64")

    // web 依赖 business-data，为了方便业务处理
    implementation(project(":business:business-data"))
}