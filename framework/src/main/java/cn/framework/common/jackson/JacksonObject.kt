package cn.framework.common.jackson

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDateTime
import java.util.function.Consumer
import java.util.function.Function

/**
 * 封装 ObjectNode 的一些操作，贴近 fastjson 的写法
 *
 *
 * 使用了 jackson 的指定序列化反序列化函数，使用起来更贴近一个正常的对象，符合 fastjson 的操作习惯
 *
 * @author zxd
 */
class JacksonObject(private val objectNode: ObjectNode = createObjectNode()) : Jackson() {

    /**
     * 序列化指定函数
     *
     * @return 序列化结果
     */
    @JsonValue
     fun serialization(): ObjectNode {
        return objectNode
    }

    /**
     * 是否为空
     *
     * @return true or false
     */
    fun isEmpty(): Boolean = objectNode.isEmpty

    /**
     * 判断是否为空
     *
     * @return true or false
     */
    fun size() = objectNode.size()

    /**
     * 判断 key 是否存在，当使用 [JacksonObject.put] value 为 null时，该方法返回 true
     *
     * @param key key
     * @return true or false
     */
    operator fun contains(key: String): Boolean {
        return objectNode.has(key)
    }

    /**
     * 判断多个 key 是否存在
     *
     * @param keys keys
     * @return true or false
     */
    fun containsAny(vararg keys: String): Boolean {
        for (key in keys) {
            if (objectNode.has(key)) {
                return true
            }
        }
        return false
    }

    /**
     * 支持  JacksonObject()[]
     */
    private operator fun get(key: String): Any? {
        return this.getObject(key)
    }

    /**
     * 通过 key 获取封装的 JacksonObject
     *
     *
     * 默认 key 的 value 是 ObjectNode 类型，否则会抛出异常类型转换异常，符合 fastjson 的使用习惯
     *
     * @param key key
     * @return 封装的 JacksonObject
     */
    fun getJacksonObject(key: String): JacksonObject? {
        val value = objectNode[key] ?: return null
        return if (value.isObject) {
            JacksonObject(value as ObjectNode)
        } else JacksonObject(OBJECT_MAPPER.valueToTree(value.asText()))
    }

    /**
     * 通过 key 获取封装的 JacksonArray
     *
     *
     * 默认 key 的 value 是 ArrayNode 类型，否则会抛出异常类型转换异常，符合 fastjson 的使用习惯
     *
     * @param key key
     * @return 封装的 JacksonArray
     */
    fun getJacksonArray(key: String): JacksonArray? {
        val value = objectNode[key] ?: return null
        return if (value.isArray) {
            JacksonArray((value as ArrayNode))
        } else JacksonArray(OBJECT_MAPPER.valueToTree(value.asText()))
    }

    /**
     * 通过 key 获取任意类型的数据
     *
     * @param key key
     * @return value
     */
    fun getObject(key: String): Any? {
        val jsonNode = objectNode[key] ?: return null
        return OBJECT_MAPPER.convertValue(jsonNode, Any::class.java)
    }

    /**
     * 通过 key 获取 jackson 原生的 JsonNode
     *
     *
     * JsonNode 的类型不仅囊括了 Object Array，还有一些比如 integer double 等一些基本类型，
     * 甚至包括 NullNode，MissingNode 类型，使用上比较全面，因此暴露出来.
     *
     * @param key key
     * @return JsonNode
     */
    fun getNode(key: String): JsonNode? {
        return objectNode[key]
    }

    /**
     * 通过 key 获取 java 对象
     *
     * @param key key
     * @return java 对象
     */
    fun <T> getObject(key: String, clazz: Class<T>): T? {
        val jsonNode = objectNode[key] ?: return null
        return OBJECT_MAPPER.convertValue(jsonNode, clazz)
    }

    fun <T> getJavaObject(key: String): T? {
        val jsonNode = objectNode[key] ?: return null;
        return OBJECT_MAPPER.convertValue(jsonNode, object : TypeReference<T>() {})
    }

    /**
     * 通过 key 获取 java 对象
     *
     * @param key           key
     * @param typeReference 能够嵌套模版转化，比如 new TypeReference< Map< String, String>>(){}
     * @return java 对象
     */
    fun <T> getObject(key: String, typeReference: TypeReference<T>): T {
        return OBJECT_MAPPER.convertValue(objectNode[key], typeReference)
    }

    /**
     * 通过 key 获取 boolean
     *
     *
     * 如果原生对象不是 boolean 类型，以是否有值提供 true or false
     *
     * @param key key
     * @return boolean
     */
    fun getBoolean(key: String): Boolean? {
        val value = objectNode[key] ?: return null
        return if (value.isBoolean) {
            value.asBoolean()
        } else OBJECT_MAPPER.convertValue(value, Boolean::class.javaPrimitiveType)
    }

    /**
     * 通过 key 获取 byte 数组
     *
     * @param key key
     * @return byte 数组
     */
    fun getBytes(key: String): ByteArray? {
        val value = objectNode[key] ?: return null
        return if (value.isBinary) {
            value.binaryValue()
        } else {
            OBJECT_MAPPER.convertValue(value, ByteArray::class.java)
        }
    }

    /**
     * 通过 key 获取 short，会经过类型转换，不存在返回 0
     *
     * @param key key
     * @return short
     */
    fun shortValue(key: String): Short {
        val value = objectNode[key] ?: return 0
        return if (value.canConvertToInt()) {
            value.shortValue()
        } else OBJECT_MAPPER.convertValue(
            value,
            Short::class.javaPrimitiveType
        )
    }

    /**
     * 通过 key 获取 int，会经过类型转换，不存在返回 0
     *
     * @param key key
     * @return int
     */
    fun intValue(key: String): Int {
        val value = objectNode[key] ?: return 0
        return if (value.canConvertToInt()) {
            value.intValue()
        } else OBJECT_MAPPER.convertValue(value, Int::class.javaPrimitiveType)
    }

    /**
     * 通过 key 获取 long，会经过类型转换，不存在返回 0
     *
     * @param key key
     * @return long
     */
    fun longValue(key: String): Long {
        val value = objectNode[key] ?: return 0L
        return if (value.canConvertToLong()) {
            value.longValue()
        } else OBJECT_MAPPER.convertValue(value, Long::class.javaPrimitiveType)
    }

    /**
     * 通过 key 获取 float，会经过类型转换，不存在返回 0.0
     *
     * @param key key
     * @return float
     */
    fun floatValue(key: String): Float {
        val value = objectNode[key] ?: return 0F
        return if (value.isFloatingPointNumber) {
            value.floatValue()
        } else OBJECT_MAPPER.convertValue(
            value,
            Float::class.javaPrimitiveType
        )
    }

    /**
     * 通过 key 获取 double，会经过类型转换，不存在返回 0.0
     *
     * @param key key
     * @return double
     */
    fun doubleValue(key: String?): Double {
        val value = objectNode[key] ?: return 0.0
        return if (value.canConvertToInt()) {
            value.doubleValue()
        } else OBJECT_MAPPER.convertValue(
            value,
            Double::class.javaPrimitiveType
        )
    }

    /**
     * 通过 key 获取 BigDecimal，会经过类型转换，不存在返回 null
     *
     * @param key key
     * @return BigDecimal
     */
    fun getBigDecimal(key: String?): BigDecimal? {
        val value = objectNode[key] ?: return null
        return if (value.isBigDecimal) {
            value.decimalValue()
        } else OBJECT_MAPPER.convertValue(value, BigDecimal::class.java)
    }

    /**
     * 通过 key 获取 BigInteger，会经过类型转换，不存在返回 null
     *
     * @param key key
     * @return BigInteger
     */
    fun getBigInteger(key: String?): BigInteger? {
        val value = objectNode[key] ?: return null
        return if (value.canConvertToInt()) {
            value.bigIntegerValue()
        } else OBJECT_MAPPER.convertValue(value, BigInteger::class.java)
    }

    /**
     * 通过 key 获取 string，会经过类型转换，不存在返回 null
     *
     *
     * 虽然 ObjectNode 获取不存在情况下回返回 NullNode，永远不会返回 null，但是 asText 时会返回字符串 "null"
     * 可能会产生误解，导致问题难以排查
     *
     * @param key key
     * @return string
     */
    fun getString(key: String?): String? {
        return objectNode[key]?.let {
            var str = it.asText()
            if (str.isNullOrEmpty()) {
                str = it.toString()
            }
            str
        }
    }

    /**
     * 通过 key 获取 LocalDateTime，会经过类型转换，不存在返回 null
     *
     *
     * 使用 LocalDateTime 代替 Date
     *
     * @param key key
     * @return LocalDateTime
     */
    fun getDateTime(key: String): LocalDateTime? {
        val value = objectNode[key] ?: return null
        return OBJECT_MAPPER.convertValue(value, LocalDateTime::class.java)
    }

    /**
     * 添加键值对，值可以 null
     *
     * @param key   key
     * @param value 任意值
     * @return 自身 JacksonObject
     */
    fun put(key: String, value: Any?): JacksonObject {
        objectNode.replace(key, OBJECT_MAPPER.valueToTree(value))
        return this
    }

    /**
     * 映射值
     *
     * @param key      key
     * @param function 函数
     * @return 映射值
     */
    fun <T> map(key: String, function: Function<JsonNode?, T>): T {
        return function.apply(objectNode[key])
    }

    /**
     * 存在操作
     *
     * @param key      key
     * @param consumer 函数
     */
    fun ifPresent(key: String, consumer: Consumer<JsonNode>) {
        if (objectNode.has(key)) {
            consumer.accept(objectNode[key])
        }
    }

    /**
     * 移除元素
     *
     * @param key key
     * @return 自身 JacksonObject
     */
    fun remove(vararg key: String): JacksonObject {
        objectNode.remove(key.toList())
        return this
    }

    fun putAll(jsonObject: JacksonObject): JacksonObject {
        objectNode.setAll<ObjectNode>(jsonObject.objectNode)
        return this
    }

    /**
     * 获取 key 迭代器
     *
     * @return
     */
    fun keys(): Iterator<String> {
        return objectNode.fieldNames()
    }

    operator fun set(key: String, value: Any?) {
        put(key, value)
    }

    companion object {
        /**
         * 反序列化指定函数
         *
         * @param value 反序列化数据来源
         * @return 封装的 JacksonObject 对象
         */
        @JvmStatic
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        private fun deserialization(value: Map<String, Any>): JacksonObject {
            return JacksonObject(OBJECT_MAPPER.valueToTree(value))
        }
    }
}