plugins {
    id("org.bytedeco.gradle-javacpp-platform") version "1.5.5"
}

/**
 * javacpp 配置目标平台
 * 主要使用：linux-x86_64, macosx-x86_64, windows-x86_64
 * 其它平台： android-arm, android-arm64, android-x86, ios-arm, ios-arm64, ios-x86,
 * ios-x86_64, linux-armhf, linux-arm64, linux-ppc64le, linux-x86, windows-x86
 */
//ext["javacppPlatform"] = "macosx-x86_64"

tasks {
    build {
        enabled = false
    }
}

subprojects {
    apply {
        plugin("org.bytedeco.gradle-javacpp-platform")
    }

    val javacppVersion = "1.5.7"
    val javacppFfmpegVersion = "5.0"
    val elasticsearchVersion = "7.10.2"

    ext["elasticsearch.version"] = elasticsearchVersion

    dependencyManagement {
        dependencies {
            dependency(
                mapOf("group" to "org.bytedeco", "name" to "javacpp", "version" to javacppVersion)
            )
            dependency(
                mapOf(
                    "group" to "org.bytedeco",
                    "name" to "ffmpeg-platform",
                    "version" to "${javacppFfmpegVersion}-${javacppVersion}"
                )
            )
            dependency(
                mapOf("group" to "org.bytedeco", "name" to "javacv", "version" to javacppVersion)
            ) {
                exclude(mapOf("group" to "*", "name" to "*"))
            }

            dependency(mapOf("group" to "com.alibaba", "name" to "easyexcel", "version" to "3.1.0"))
            dependency(mapOf("group" to "jakarta.json", "name" to "jakarta.json-api", "version" to "2.0.1"))
        }
    }

    dependencies {
        /* business 下所有项目依赖 business-foundation 并避免循环依赖 */
        allprojects.forEach { project ->
            if (project.name != "business-foundation") {
                implementation(project(":business:business-foundation"))
            }
        }

        /* business 下所有项目为传统 mvc 项目，排除 tomcat 依赖，防止打包时包含 */
        implementation(group = "org.springframework.boot", name = "spring-boot-starter-web") {
            exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
        }
        /* 运行时依赖 tomcat, 防止打包时包含 */
        runtimeOnly(group = "org.springframework.boot", name = "spring-boot-starter-tomcat")
        /* 补充 tomcat 缺失导致的 servlet-api 缺少，运行容器中已经包含，不需要打包时包含 */
        compileOnly(group = "javax.servlet", name = "javax.servlet-api")
        testCompileOnly(group = "javax.servlet", name = "javax.servlet-api")

        implementation(group = "org.springframework.boot", name = "spring-boot-starter-validation")

    }
}