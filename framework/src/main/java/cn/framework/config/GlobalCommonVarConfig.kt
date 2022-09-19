package cn.framework.config

import cn.framework.common.http.InnerExp
import cn.framework.common.log.Slf4k.Companion.log
import cn.framework.config.GlobalCommonVarConfig.Companion.NAMESPACE_FILE_PATH
import com.alibaba.cloud.nacos.NacosConfigBootstrapConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import java.io.File
import java.util.Objects
import javax.annotation.PostConstruct

/**
 * 规范 nacos 与 arthas 变量的设置，将它们生成的文件，
 * 统一配置在 [GlobalCommonVarConfig.SERVER_COMMON_BASE_KEY] 中
 *
 * 利用[NAMESPACE_FILE_PATH]确定集群名称，使用相同名称的服务才能够
 * 发现对方，方便本地测试，与其它测试环境隔离
 *
 * @author zxd
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(NacosConfigBootstrapConfiguration::class)
@AutoConfigureAfter(Environment::class)
class GlobalCommonVarConfig(private val environment: Environment) {
    private companion object {
        const val NAMESPACE_KEY = "nacosNamespace"
        val NAMESPACE_FILE_PATH = "\${user.home}${File.separator}.server.namespace"
        const val APPLICATION_NAME_KEY = "spring.application.name"
        const val SERVER_COMMON_BASE_KEY = "SERVER_COMMON.BASE"
        val SERVER_COMMON_BASE_VALUE = "\${user.home}${File.separator}common"
        const val ARTHAS_LOG_PATH_KEY = "ARTHAS_LOG_PATH"
        val ARTHAS_LOG_PATH_VALUE =
            "\${SERVER_COMMON.BASE:-\${user.home}}${File.separator}common${File.separator}logs${File.separator}arthas${File.separator}\${spring.application.name}"
        const val ARTHAS_OUTPUT_PATH_KEY = "arthas.outputPath"
        val ARTHAS_OUTPUT_PATH_VALUE =
            "\${SERVER_COMMON.BASE:-\${user.home}}${File.separator}common${File.separator}arthas-output${File.separator}\${spring.application.name}"
        const val RESULT_LOG_FILE_KEY = "RESULT_LOG_FILE"
        val RESULT_LOG_FILE_VALUE =
            "\${SERVER_COMMON.BASE:-\${user.home}}${File.separator}common${File.separator}logs${File.separator}arthas-cache${File.separator}\${spring.application.name}${File.separator}\${spring.application.name}-result.log"
        const val JM_LOG_PATH_KEY = "JM.LOG.PATH"
        val JM_LOG_PATH_VALUE =
            "\${SERVER_COMMON.BASE:-\${user.home}}${File.separator}common${File.separator}logs${File.separator}nacos${File.separator}\${spring.application.name}"
        const val JM_SNAPSHOT_PATH_KEY = "JM.SNAPSHOT.PATH"
        val JM_SNAPSHOT_PATH_VALUE = "\${SERVER_COMMON.BASE}${File.separator}config"
    }

    @PostConstruct
    fun initEnv() {
        if (!environment.containsProperty(NAMESPACE_KEY)) {
            val file = File(environment.resolvePlaceholders(NAMESPACE_FILE_PATH))
            if (file.exists()) {
                val namespace = file.readText().trim { it <= ' ' }
                InnerExp.ifThr(namespace.isEmpty(), "namespace not config")
                System.setProperty(NAMESPACE_KEY, namespace)
            } else {
                log.warn("namespace config file not found : {}", file)
            }
        }
        System.setProperty(APPLICATION_NAME_KEY, Objects.requireNonNull(environment.getProperty(APPLICATION_NAME_KEY)))
        if (!environment.containsProperty(SERVER_COMMON_BASE_KEY)) {
            System.setProperty(SERVER_COMMON_BASE_KEY, environment.resolvePlaceholders(SERVER_COMMON_BASE_VALUE))
        }
        System.setProperty(ARTHAS_LOG_PATH_KEY, environment.resolvePlaceholders(ARTHAS_LOG_PATH_VALUE))
        System.setProperty(ARTHAS_OUTPUT_PATH_KEY, environment.resolvePlaceholders(ARTHAS_OUTPUT_PATH_VALUE))
        System.setProperty(RESULT_LOG_FILE_KEY, environment.resolvePlaceholders(RESULT_LOG_FILE_VALUE))
        System.setProperty(JM_LOG_PATH_KEY, environment.resolvePlaceholders(JM_LOG_PATH_VALUE))
        System.setProperty(JM_SNAPSHOT_PATH_KEY, environment.resolvePlaceholders(JM_SNAPSHOT_PATH_VALUE))
    }
}