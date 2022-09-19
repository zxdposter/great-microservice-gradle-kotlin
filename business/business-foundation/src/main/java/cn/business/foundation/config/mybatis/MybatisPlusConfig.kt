package cn.business.foundation.config.mybatis

import cn.hutool.core.text.CharSequenceUtil
import cn.framework.common.log.Slf4k.Companion.log
import com.baomidou.mybatisplus.annotation.DbType
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration
import com.baomidou.mybatisplus.core.MybatisConfiguration
import com.baomidou.mybatisplus.extension.MybatisMapWrapperFactory
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import javax.annotation.PostConstruct

/**
 * mybatis-plus 配置类
 *
 * @author GS
 * @since 2021/3/8 16:43
 */
@Configuration
@AutoConfigureAfter(MybatisPlusAutoConfiguration::class)
@ConditionalOnClass(MybatisPlusAutoConfiguration::class)
class MybatisPlusConfig(private val objectMapperBuilder: Jackson2ObjectMapperBuilder) {
    companion object {
        private val DEFAULT_BD_TYPE = DbType.MYSQL
    }

    /**
     * 用于利用 mybatis plus 存储复杂对象
     *
     * mybatis plus 使用 项目中配置过得 objectMapper，因为项目中的 jackson 添加过 KotlinModule，
     * 在序列化 data class 时不会出错
     *
     * mybatis plus 在这方面支持的不是特别完善，用的是静态变量，因此只能这样做
     */
    @PostConstruct
    fun init() {
        JacksonTypeHandler.setObjectMapper(objectMapperBuilder.build())
    }

    /**
     * 根据 mybatis-plus 提供的配置，定制化配置数据
     */
    @Value("\${mybatis-plus.configuration.database-id:}")
    private lateinit var dataBaseId: String

    /**
     * 新的分页插件
     */
    @Bean
    fun mybatisPlusInterceptor(): MybatisPlusInterceptor {
        var dbType: DbType? = DEFAULT_BD_TYPE
        if (CharSequenceUtil.isNotBlank(dataBaseId)) {
            val type = DbType.getDbType(dataBaseId)
            if (type == null) {
                log.warn(
                    "database id {} is wrong, choose default database {}",
                    dataBaseId,
                    DEFAULT_BD_TYPE
                )
            } else {
                dbType = type
            }
        }
        val interceptor = MybatisPlusInterceptor()
        interceptor.addInnerInterceptor(PaginationInnerInterceptor(dbType))
        return interceptor
    }

    /**
     * 返回map解决下划线不转驼峰的问题
     */
    @Bean
    fun configurationCustomizer(): ConfigurationCustomizer {
        return ConfigurationCustomizer { configuration: MybatisConfiguration ->
            configuration.objectWrapperFactory = MybatisMapWrapperFactory()
        }
    }


}