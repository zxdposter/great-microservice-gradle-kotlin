package cn.business.foundation.config.mybatis

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.core.enums.SqlMethod
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.baomidou.mybatisplus.core.metadata.TableInfo
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper
import com.baomidou.mybatisplus.core.toolkit.Assert
import com.baomidou.mybatisplus.core.toolkit.Constants
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit
import com.baomidou.mybatisplus.core.toolkit.StringUtils
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtQueryChainWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateChainWrapper
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper
import org.apache.ibatis.binding.MapperMethod.ParamMap
import org.apache.ibatis.logging.Log
import org.apache.ibatis.logging.LogFactory
import org.apache.ibatis.session.SqlSession
import org.apache.ibatis.type.UnknownTypeHandler
import org.springframework.cglib.proxy.Enhancer
import org.springframework.cglib.proxy.MethodInterceptor
import org.springframework.cglib.proxy.MethodProxy
import org.springframework.transaction.annotation.Transactional
import java.io.Serializable
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.util.Optional
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KProperty
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.jvmName

/**
 * BaseMapper 的增强实现
 */
interface PBaseMapper<T : Any> : BaseMapper<T> {
    companion object {
        private val log: Log = LogFactory.getLog(PBaseMapper::class.java)
    }

    /**
     * 提供给 kotlin 中使用
     */
    fun ktQuery(): KtQueryChainWrapper<T> {
        return ChainWrappers.ktQueryChain(this, currentModelClass())
    }

    /**
     * 提供给 kotlin 中使用
     */
    fun ktUpdate(): KtUpdateChainWrapper<T> {
        val e = Enhancer()
        e.classLoader = this.javaClass.classLoader
        e.setSuperclass(KtUpdateChainWrapper::class.java)
        e.setCallback(object : MethodInterceptor {
            @Throws(Throwable::class)
            override fun intercept(obj: Any, method: Method, args: Array<Any?>, proxy: MethodProxy): Any {
                if (method.name != "set" || method.parameterTypes.size != 4) {
                    return proxy.invokeSuper(obj, args)
                }
                val condition = args[0] as Boolean
                if (!condition) {
                    return proxy.invokeSuper(obj, args)
                }
                val tableField = (args[1] as KProperty<*>).javaField!!.annotations.find {
                    it.annotationClass.isSubclassOf(TableField::class)
                } as TableField? ?: return proxy.invokeSuper(obj, args)
                (args[3] as String?)?.let {
                    if (it.contains("typeHandler") || tableField.typeHandler.isSubclassOf(UnknownTypeHandler::class)) {
                        return proxy.invokeSuper(obj, args)
                    } else {
                        args[3] = it + ",typeHandler=" + tableField.typeHandler.jvmName
                    }
                } ?: run {
                    args[3] = "typeHandler=" + tableField.typeHandler.jvmName
                }
                return proxy.invokeSuper(obj, args)
            }
        })
        return e.create(
            arrayOf(BaseMapper::class.java, Class::class.java),
            arrayOf(this, currentModelClass())
        ) as KtUpdateChainWrapper<T>
    }

    fun query(): QueryChainWrapper<T>? {
        return ChainWrappers.queryChain(this)
    }

    fun update(): UpdateChainWrapper<T>? {
        return ChainWrappers.updateChain(this)
            .setEntityClass(currentModelClass())
    }

    /**
     * 批量新增，默认单个批次 1000
     *
     * @param entityList 实体数据
     * @return 操作数量
     */
    fun insertBatch(entityList: Collection<T>?): Int {
        return insertBatch(entityList, 1000)
    }

    /**
     * 批量新增
     *
     * @param entityList 实体数据
     * @param batchSize  单批次数量
     * @return 操作数量
     */
    @Transactional(rollbackFor = [Exception::class])
    fun insertBatch(entityList: Collection<T>?, batchSize: Int): Int {
        val sqlStatement = getSqlStatement(SqlMethod.INSERT_ONE)
        val insertCount = AtomicInteger(0)
        SqlHelper.executeBatch(
            currentModelClass(), log, entityList, batchSize
        ) { sqlSession: SqlSession, t: T -> insertCount.getAndAdd(sqlSession.insert(sqlStatement, t)) }
        return insertCount.get()
    }

    /**
     * 批量新增，默认单个批次 1000
     *
     * @param entityList 实体数据
     * @return 操作数量
     */
    fun insertOrUpdateBatch(entityList: Collection<T>?): Int {
        return insertOrUpdateBatch(entityList, 1000)
    }

    /**
     * 批量新增
     *
     * @param entityList 实体数据
     * @param batchSize  单批次数量
     * @return 操作数量
     */
    @Transactional(rollbackFor = [Exception::class])
    fun insertOrUpdateBatch(entityList: Collection<T>?, batchSize: Int): Int {
        val tableInfo = TableInfoHelper.getTableInfo(currentModelClass())
        Assert.notNull(tableInfo, "error: can not execute. because can not find cache of TableInfo for entity!")
        val keyProperty = tableInfo.keyProperty
        Assert.notEmpty(keyProperty, "error: can not execute. because can not find column for id from entity!")
        val updateSqlStatement = getSqlStatement(SqlMethod.UPDATE_BY_ID)
        val insertSqlStatement = getSqlStatement(SqlMethod.INSERT_ONE)
        val count = AtomicInteger(0)
        SqlHelper.executeBatch(currentModelClass(), log, entityList, batchSize) { sqlSession: SqlSession, entity: T ->
            val idVal = ReflectionKit.getFieldValue(entity, keyProperty)
            val param = ParamMap<T>()
            param[Constants.ENTITY] = entity
            if (StringUtils.checkValNull(idVal)) {
                val ret = sqlSession.update(updateSqlStatement, param)
                if (ret > 0) {
                    count.addAndGet(ret)
                    return@executeBatch
                }
            }
            count.addAndGet(sqlSession.insert(insertSqlStatement, param))
        }
        return count.get()
    }

    /**
     * 批量新增，默认单个批次 1000
     *
     * @param entityList 实体数据
     * @return 操作数量
     */
    fun updateBatchById(entityList: Collection<T>?): Int {
        return updateBatchById(entityList, 1000)
    }

    /**
     * 批量更新
     *
     * @param entityList 实体数据
     * @param batchSize  单批次数量
     * @return 操作数量
     */
    @Transactional(rollbackFor = [Exception::class])
    fun updateBatchById(entityList: Collection<T>?, batchSize: Int): Int {
        val sqlStatement = getSqlStatement(SqlMethod.UPDATE_BY_ID)
        val count = AtomicInteger(0)
        SqlHelper.executeBatch(currentModelClass(), log, entityList, batchSize) { sqlSession: SqlSession, entity: T ->
            val param = ParamMap<T>()
            param[Constants.ENTITY] = entity
            count.addAndGet(sqlSession.update(sqlStatement, param))
        }
        return count.get()
    }

    /**
     * 新增或者更新.
     *
     * @param entity 实体
     * @return 操作数量
     */
    @Transactional(rollbackFor = [Exception::class])
    fun insertOrUpdate(entity: T?): Int {
        if (null != entity) {
            val tableInfo: TableInfo = TableInfoHelper.getTableInfo(entity.javaClass)
            Assert.notNull(tableInfo, "error: can not execute. because can not find cache of TableInfo for entity!")
            val keyProperty = tableInfo.keyProperty
            Assert.notEmpty(keyProperty, "error: can not execute. because can not find column for id from entity!")
            val idVal = ReflectionKit.getFieldValue(entity, keyProperty)
            if (StringUtils.checkValNull(idVal)) {
                val ret = updateById(entity)
                if (ret > 0) {
                    return ret
                }
            }
            return insert(entity)
        }
        return 0
    }

    /**
     * 返回当前 mapper 的模版类对象.
     *
     * @return T.class
     */
    fun currentModelClass(): Class<T> {
        val baseMapperClassTypeName = javaClass.genericInterfaces[0].typeName
        val baseMapperClass = Class.forName(baseMapperClassTypeName)
        val parameterizedType = baseMapperClass.genericInterfaces[0] as ParameterizedType
        val entityClassName = parameterizedType.actualTypeArguments[0].typeName
        return Class.forName(entityClassName) as Class<T>
    }

    /**
     * 获取mapperStatementId
     *
     * @param sqlMethod 方法名
     * @return 命名id
     * @since 3.4.0
     */
    fun getSqlStatement(sqlMethod: SqlMethod): String {
        return SqlHelper.table(currentModelClass()).getSqlStatement(sqlMethod.method)
    }

    fun optionalSelectById(id: Serializable?): Optional<T> {
        return Optional.ofNullable(selectById(id))
    }
}