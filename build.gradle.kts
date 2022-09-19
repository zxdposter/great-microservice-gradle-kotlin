/**
 * 为全局子项目做统一配置，包括
 * 子项目共同依赖
 * 子项目依赖版本
 * 子项目打包配置
 * 子项目测试平台
 * 子项目打包版本
 */
import com.palantir.gradle.gitversion.VersionDetails

/* 主要依赖版本 */
val springCloudVersion = "2021.0.3"
val springAlibabaVersion = "2021.1"
val springBootStarterVersion = "2.7.1"
val springRedissonVersion = "3.17.5"
val springKnife4jVersion = "3.0.3"
val springMybatisPlusVersion = "3.5.2"
val hutoolVersion = "5.6.3"
val caffeineVersion = "2.9.3"
val kotlinLoggingVersion = "2.1.23"

System.setProperty("log4j2.version", "2.18.0")

plugins {
    id("org.springframework.boot") version "2.7.1"
    id("org.jetbrains.kotlin.jvm") version "1.7.0"
    id("org.jetbrains.kotlin.plugin.spring") version "1.6.10"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("com.palantir.git-version") version "0.12.3"
    war
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}
allprojects {
    apply {
        plugin("com.palantir.git-version")
        // 能够覆盖 spring 自带的默认依赖版本工具，https://www.baeldung.com/spring-boot-override-dependency-versions
        plugin("io.spring.dependency-management")
    }
    val versionDetails: groovy.lang.Closure<VersionDetails> by extra
    group = "cn.server"
    version = versionDetails().gitHash

    repositories {
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
    }

}

subprojects {
    apply {
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
        plugin("org.jetbrains.kotlin.plugin.spring")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("com.palantir.git-version")
        plugin("war")
    }

    java.sourceCompatibility = JavaVersion.VERSION_1_8
    java.targetCompatibility = JavaVersion.VERSION_1_8

    /* 子项目版本管理 */
    dependencyManagement {
        imports {
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}")
            mavenBom("com.alibaba.cloud:spring-cloud-alibaba-dependencies:${springAlibabaVersion}")
        }

        dependencies {
            dependencySet(
                mapOf("group" to "com.github.xiaoymin", "version" to springKnife4jVersion)
            ) {
                entry("knife4j-spring-boot-starter")
                entry("knife4j-micro-spring-boot-starter")
            }
            // 解决在 macos 下 netty 报的 dns 错误，报错不影响使用，其它平台不依赖
            dependency(
                mapOf("group" to "io.netty", "name" to "netty-resolver-dns-native-macos", "version" to "4.1.78.Final")
            )
            dependency(
                mapOf(
                    "group" to "com.baomidou",
                    "name" to "mybatis-plus-boot-starter",
                    "version" to springMybatisPlusVersion
                )
            )
        }
    }
    /* 全局子项目依赖设置 */
    dependencies {
        /* 所有项目依赖 framework 并避免循环依赖 */
        allprojects.forEach { project ->
            if (project.name != "framework") {
                implementation(project(":framework"))
            }
        }
        /* 工具类库 */
        implementation(group = "cn.hutool", name = "hutool-all", version = hutoolVersion)
        /* 配置注解处理 */
        annotationProcessor(group = "org.springframework.boot", name = "spring-boot-configuration-processor")
        testImplementation(group = "org.springframework.boot", name = "spring-boot-starter-test")
        /* spring security 框架 */
        implementation(
            group = "org.springframework.boot",
            name = "spring-boot-starter-security",
            version = springBootStarterVersion
        )
        /* redisson 配合 spring data session 做 session 管理 */
        implementation(group = "org.springframework.session", name = "spring-session-data-redis")
        /* redisson redis 客户端 */
        implementation(group = "org.redisson", name = "redisson-spring-boot-starter", version = springRedissonVersion) {
            isTransitive = true
            exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
        }
        /* 服务器负载 */
        implementation(group = "org.springframework.cloud", name = "spring-cloud-starter-loadbalancer")
        /* 使 bootstrap.yml 配置生效 */
        implementation(group = "org.springframework.cloud", name = "spring-cloud-starter-bootstrap")
        /* http 客户端 */
        implementation(group = "org.springframework.cloud", name = "spring-cloud-starter-openfeign")
        /* alibaba 配置服务器客户端 */
        implementation(group = "com.alibaba.cloud", name = "spring-cloud-starter-alibaba-nacos-config") {
            isTransitive = true
            exclude(group = "com.google.errorprone", module = "error_prone_annotations")
            exclude(group = "com.google.guava", module = "guava")
        }
        /* alibaba 服务发现 */
        implementation(group = "com.alibaba.cloud", name = "spring-cloud-starter-alibaba-nacos-discovery") {
            isTransitive = true
            exclude(group = "org.springframework.cloud", module = "spring-cloud-netflix-archaius")
            exclude(group = "com.netflix.servo", module = "servo-core")
            exclude(group = "com.netflix.archaius", module = "archaius-core")
            exclude(group = "com.google.guava", module = "guava")
            exclude(group = "org.springframework.cloud", module = "spring-cloud-starter-netflix-ribbon")
        }
        /* kotlin 映射工具 */
        implementation(group = "org.jetbrains.kotlin", name = "kotlin-reflect")

        /* 文档工具需要使用的 actuator */
        implementation(group = "org.springframework.boot", name = "spring-boot-actuator-autoconfigure")

        /* 缓存框架 */
        implementation(group = "org.springframework.boot", name = "spring-boot-starter-cache")
        implementation(group = "com.github.ben-manes.caffeine", name = "caffeine", version = caffeineVersion)

        /* jackson 模块 */
        implementation(group = "com.fasterxml.jackson.datatype", name = "jackson-datatype-jsr310")
        implementation(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin")

        implementation(group = "io.github.microutils", name = "kotlin-logging-jvm", version = kotlinLoggingVersion)
    }

    /* 拷贝 java 目录下的所有 xml，一般为 mybatis 的 mapper xml */
    copy {
        from("src/main/java")
        include("**/*.xml")
        into("${buildDir}/classes/kotlin/main")
    }


    /* 所有项目任务设置 */
    tasks {
        compileKotlin {
            kotlinOptions {
                jvmTarget = "1.8"
                javaParameters = true
                freeCompilerArgs =
                    listOf("-Xjvm-default=all", "-Xemit-jvm-type-annotations")
            }
        }
        compileTestKotlin {
            kotlinOptions {
                jvmTarget = "1.8"
                javaParameters = true
            }
        }

        /* 打包时关闭测试 */
        gradle.taskGraph.whenReady {
            if (hasTask("build")) {
                getByName("test").enabled = false
            }
        }

        /* 是否开启构建 */
        build {
            enabled = true
        }

        /* 全局 spring boot 打包设置 */
        bootJar {
            /* 默认不打包 */
            enabled = false
            /* 设置默认 jar 名称 */
            archiveFileName.set("${archiveBaseName.get()}-spring-boot-${archiveVersion.get()}.${archiveExtension.get()}")
            /* 所有项目排除 bootstrap.yml，必须外置 */
            exclude("bootstrap.yml", "application*")
        }

        /* 全局 jar 打包设置 */
        jar {
            /* 默认不打包 */
            enabled = false
            archiveFileName.set("${archiveBaseName.get()}-${archiveVersion.get()}.${archiveExtension.get()}")
            /* 所有项目排除 bootstrap.yml，必须外置 */
            exclude("bootstrap.yml")
        }

        /* 全局 war 打包设置 */
        "war"(War::class) {
            enabled = false
            archiveFileName.set("${archiveBaseName.get()}-${archiveVersion.get()}.${archiveExtension.get()}")
            /* 所有项目排除项目编译产生的 class */
            rootSpec.exclude("cn")
            /* 所有项目排除 bootstrap.yml，必须外置 */
            rootSpec.exclude("bootstrap.yml")
            /* 将编译出来的 jar 打包进入 war 内 */
            from(jar) {
                into("WEB-INF/lib")
            }
            /* 把 web.xml 与 weblogic.xml 打包 */
            webInf {
                from("src/main/resources/web.xml")
                from("src/main/resources/weblogic.xml")
            }
        }

        /* 注册 buildAll 任务，能够从顶至下的调用所有 project 的 bootJar jar war */
        register(name = "buildAll") {
            val packType = listOf("bootJar", "jar", "war")
            val preTaskList = mutableListOf<String>()
            if (project.hasProperty("pack")) {
                val pack: String by project
                pack.split(",").forEach { p ->
                    if (packType.contains(p)) {
                        preTaskList.add(p)
                    }
                }
            } else {
                preTaskList.addAll(packType)
            }

            val taskList = mutableListOf(project.tasks.findByName("clean") as Task)

            fun predict(project: Project) {
                val buildTask = project.tasks.findByName("build") as Task
                if (buildTask.enabled) {
                    preTaskList.forEach { preTask ->
                        taskList.add(project.tasks.findByName("clean") as Task)
                        val taskByName = project.tasks.findByName(preTask) as Task
                        if (taskByName.enabled) {
                            /* 此处很重要，这个任务必须依赖前一个任务，否则执行顺序会乱，导致 clean 后面执行 */
                            taskByName.mustRunAfter(taskList[taskList.size - 1])
                            taskList.add(taskByName)
                        }
                    }
                }
            }

            predict(project)
            project.subprojects.forEach { subProject ->
                predict(subProject)
            }

            dependsOn(taskList)
        }

        /* 指定测试平台 */
        test {
            useJUnitPlatform()
        }
    }
}

tasks {
    build {
        enabled = false
    }
}
