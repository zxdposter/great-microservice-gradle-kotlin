package cn.business.foundation.config.mybatis

import cn.framework.common.log.Slf4k.Companion.log
import cn.framework.common.security.User
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler
import org.apache.ibatis.reflection.MetaObject
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Configuration
import java.time.LocalDateTime
import java.util.function.Supplier

/**
 * 公共字段填充
 *
 * @author GS
 * @since 2021/2/24 11:07
 */
@Configuration
@AutoConfigureAfter(MybatisPlusAutoConfiguration::class)
@ConditionalOnClass(MybatisPlusAutoConfiguration::class)
class MetaObjectHandlerConfig : MetaObjectHandler {
    companion object {
        private const val CREATE_USER_ID = "createUserId"
        private const val MODIFY_USER_ID = "modifyUserId"
        private const val CREATE_TIME = "createTime"
        private const val MODIFY_TIME = "modifyTime"
    }

    override fun insertFill(metaObject: MetaObject) {
        if (metaObject.hasSetter(CREATE_USER_ID)) {
            val userId = User.mvcUser()?.userid
            userId?.let {
                metaObject.setValue(CREATE_USER_ID, it)
                metaObject.setValue(MODIFY_USER_ID, it)
            } ?: log.warn("current user id is empty")
        }

        val time = LocalDateTime.now()
        if (metaObject.hasSetter(CREATE_TIME)) {
            metaObject.setValue(CREATE_TIME, time)
        }
        if (metaObject.hasSetter(MODIFY_TIME)) {
            metaObject.setValue(MODIFY_TIME, time)
        }
    }

    override fun updateFill(metaObject: MetaObject) {
        if (metaObject.hasSetter(MODIFY_USER_ID)) {
            val userId = User.mvcUser()?.userid
            userId?.let { metaObject.setValue(MODIFY_USER_ID, it) }
                ?: log.warn("current user id is empty")
        }

        if (metaObject.hasSetter(MODIFY_TIME)) {
            metaObject.setValue(MODIFY_TIME, LocalDateTime.now())
        }
    }

    /**
     * 重写strictFillStrategy(), 解决更新操作时字段不为空时不更新的问题
     */
    override fun strictFillStrategy(
        metaObject: MetaObject, fieldName: String, fieldVal: Supplier<*>
    ): MetaObjectHandler {
        val obj = fieldVal.get()
        obj?.let { metaObject.setValue(fieldName, obj) }
        return this
    }


}