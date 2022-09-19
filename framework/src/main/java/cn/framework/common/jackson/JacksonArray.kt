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

/**
 * 封装[ArrayNode]的一些操作，贴近 fastjson 的写法
 *
 *
 * 使用了 jackson 的指定序列化反序列化函数，使用起来更贴近一个正常的对象，符合 fastjson 的操作习惯
 *
 * @author zxd
 */
class JacksonArray(private var arrayNode: ArrayNode = createArrayNode()) : Jackson(),
    Iterable<JsonNode> {

    /**
     * 序列化指定函数
     *
     * @return 序列化结果
     */
    @JsonValue
    private fun serialization(): ArrayNode {
        return arrayNode
    }


    /**
     * 获取元素数量
     *
     * @return 元素数量
     */
    fun size(): Int = arrayNode.size()

    /**
     * 是否为空
     *
     * @return true or false
     */
    fun isEmpty(): Boolean = arrayNode.isEmpty

    /**
     * 判断下标是否存在
     *
     * @param index 下标
     * @return true or false
     */
    operator fun contains(index: Int): Boolean {
        return arrayNode.has(index)
    }

    /**
     * 获取迭代器，用于 for 循环
     *
     * @return 迭代器
     */
    override fun iterator(): MutableIterator<JsonNode> {
        return arrayNode.iterator()
    }

    /**
     * 添加元素，可以为 null
     *
     * @return 自身 JacksonArray
     */
    fun add(vararg e: Any): JacksonArray {
        e.forEach {
            arrayNode.add(OBJECT_MAPPER.valueToTree<JsonNode>(it))
        }
        return this
    }

    /**
     * 移除元素
     *
     * @param index 下标
     * @return 自身 JacksonArray
     */
    fun remove(index: Int): JacksonArray {
        arrayNode.remove(index)
        return this
    }

    /**
     * 移除所有元素
     *
     * @return 自身 JacksonArray
     */
    fun removeAll(): JacksonArray {
        arrayNode.removeAll()
        return this
    }

    /**
     * 添加所有元素
     *
     * @param objects 元素列表
     * @return 自身 JacksonArray
     */
    fun addAll(objects: Collection<*>): JacksonArray {
        val collect = objects.map { v: Any? -> OBJECT_MAPPER.convertValue(v, JsonNode::class.java) }
        arrayNode.addAll(collect)
        return this
    }

    /**
     * 替换元素，如果下标不合法，直接追加元素
     *
     * @param index   原来的下标
     * @param element 元素
     * @return 自身 JacksonArray
     */
    operator fun set(index: Int, element: Any?): JacksonArray {
        val jsonNode = OBJECT_MAPPER.valueToTree<JsonNode>(element)
        if (index < 0 || index >= arrayNode.size()) {
            arrayNode.add(jsonNode)
        } else {
            arrayNode[index] = jsonNode
        }
        return this
    }

    /**
     * 判断元素下标
     *
     *
     *[ArrayNode]没有实现 indexOf，需要自己写 for 循环判断
     *
     *
     * 找不到返回 -1
     *
     * @param o 目标元素
     * @return 下标
     */
    fun indexOf(o: Any?): Int {
        val jsonNode = OBJECT_MAPPER.valueToTree<JsonNode>(o)
        arrayNode.forEachIndexed { index, node ->
            if (jsonNode == node) {
                return index
            }
        }
        return -1
    }

    /**
     * 截取列表
     *
     * @param fromIndex 开始下标
     * @param toIndex   结束下标
     * @return 新列表
     */
    fun subList(fromIndex: Int, toIndex: Int): List<JsonNode> {
        val size = arrayNode.size()
        if (fromIndex < 0) {
            throw IndexOutOfBoundsException("fromIndex = $fromIndex")
        }
        if (toIndex > size) {
            throw IndexOutOfBoundsException("toIndex = $toIndex")
        }
        require(fromIndex <= toIndex) { "fromIndex($fromIndex) > toIndex($toIndex)" }
        val list: MutableList<JsonNode> = ArrayList()
        for (i in fromIndex until toIndex) {
            list.add(arrayNode[i])
        }
        return list
    }

    /**
     * 通过下标获取封装的 JacksonObject
     *
     *
     * 默认 key 的 value 是 ObjectNode 类型，否则会抛出异常类型转换异常，符合 fastjson 的使用习惯
     *
     * @param index 下标
     * @return 封装的 JacksonObject
     */
    fun getJacksonObject(index: Int): JacksonObject {
        return JacksonObject(arrayNode[index] as ObjectNode)
    }

    /**
     * 通过下标获取封装的 [JacksonArray]
     *
     *
     * 默认 key 的 value 是 [ArrayNode] 类型，否则会抛出异常类型转换异常，符合 fastjson 的使用习惯
     *
     * @param index 下标
     * @return 封装的 JacksonArray
     */
    fun getJacksonArray(index: Int): JacksonArray {
        return JacksonArray(arrayNode[index] as ArrayNode)
    }

    /**
     * 支持  JacksonArray()[]
     */
    private operator fun get(index: Int): Any? {
        return this.getObject(index)
    }

    /**
     * 通过 下标 获取任意类型的数据
     *
     * @param index 下标
     * @return value
     */
    fun getObject(index: Int): Any? {
        val jsonNode = arrayNode[index] ?: return null
        return OBJECT_MAPPER.convertValue(jsonNode, Any::class.java)
    }

    /**
     * 通过下标获取 java 对象
     *
     * @param index 下标
     * @return java 对象
     */
    fun <T> getObject(index: Int, clazz: Class<T>): T {
        val obj = arrayNode[index]
        return OBJECT_MAPPER.convertValue(obj, clazz)
    }

    /**
     * 通过下标获取 java 对象
     *
     * @param index         下标
     * @param typeReference 能够嵌套模版转化，比如 new TypeReference< Map< String, String>>(){}
     * @return java 对象
     */
    fun <T> getObject(index: Int, typeReference: TypeReference<T>): T {
        val obj = arrayNode[index]
        return OBJECT_MAPPER.convertValue(obj, typeReference)
    }

    /**
     * 通过下标获取 boolean
     *
     *
     * 如果原生对象不是 boolean 类型，以是否有值提供 true or false
     *
     * @param index 下标
     * @return boolean
     */
    fun getBoolean(index: Int): Boolean {
        val value = arrayNode[index]
        return if (value.isBoolean) {
            value.asBoolean()
        } else OBJECT_MAPPER.convertValue(value, Boolean::class.javaPrimitiveType)
    }

    /**
     * 通过下标获取 byte 数组
     *
     * @param index 下标
     * @return byte 数组
     */
    fun getBytes(index: Int): ByteArray {
        val value = arrayNode[index]
        return if (value.isBinary) {
            value.binaryValue()
        } else {
            byteArrayOf()
        }
    }

    /**
     * 通过下标获取 short，会经过类型转换，不存在返回 0
     *
     * @param index 下标
     * @return short
     */
    fun shortValue(index: Int): Short {
        val value = arrayNode[index]
        return if (value.canConvertToInt()) {
            value.shortValue()
        } else OBJECT_MAPPER.convertValue(value, Short::class.javaPrimitiveType)
    }

    /**
     * 通过下标获取 int，会经过类型转换，不存在返回 0
     *
     * @param index 下标
     * @return int
     */
    fun intValue(index: Int): Int {
        val value = arrayNode[index]
        return if (value.canConvertToInt()) {
            value.intValue()
        } else OBJECT_MAPPER.convertValue(value, Int::class.javaPrimitiveType)
    }

    /**
     * 通过下标获取 long，会经过类型转换，不存在返回 0
     *
     * @param index 下标
     * @return long
     */
    fun longValue(index: Int): Long {
        val value = arrayNode[index]
        return if (value.canConvertToLong()) {
            value.longValue()
        } else OBJECT_MAPPER.convertValue(value, Long::class.javaPrimitiveType)
    }

    /**
     * 通过下标获取 float，会经过类型转换，不存在返回 0.0
     *
     * @param index 下标
     * @return float
     */
    fun floatValue(index: Int): Float {
        val value = arrayNode[index]
        return if (value.isFloatingPointNumber) {
            value.floatValue()
        } else OBJECT_MAPPER.convertValue(value, Float::class.javaPrimitiveType)
    }

    /**
     * 通过下标获取 double，会经过类型转换，不存在返回 0.0
     *
     * @param index 下标
     * @return double
     */
    fun doubleValue(index: Int): Double {
        val value = arrayNode[index]
        return if (value.canConvertToInt()) {
            value.doubleValue()
        } else OBJECT_MAPPER.convertValue(value, Double::class.javaPrimitiveType)
    }

    /**
     * 通过下标获取 BigDecimal，会经过类型转换，不存在返回 null
     *
     * @param index 下标
     * @return BigDecimal
     */
    fun getBigDecimal(index: Int): BigDecimal {
        val value = arrayNode[index]
        return if (value.isBigDecimal) {
            value.decimalValue()
        } else OBJECT_MAPPER.convertValue(value, BigDecimal::class.java)
    }

    /**
     * 通过下标获取 BigInteger，会经过类型转换，不存在返回 null
     *
     * @param index 下标
     * @return BigInteger
     */
    fun getBigInteger(index: Int): BigInteger {
        val value = arrayNode[index]
        return if (value.canConvertToInt()) {
            value.bigIntegerValue()
        } else OBJECT_MAPPER.convertValue(value, BigInteger::class.java)
    }

    /**
     * 通过下标获取 string，会经过类型转换，不存在返回 null
     *
     *
     * 虽然[ArrayNode]获取不存在情况下回返回 NullNode，永远不会返回 null，但是 asText 时会返回字符串 "null"
     * 可能会产生误解，导致问题难以排查
     *
     * @param index 下标
     * @return string
     */
    fun getString(index: Int): String {
        val value = arrayNode[index]
        return value.asText()
    }

    /**
     * 通过下标获取 LocalDateTime，会经过类型转换，不存在返回 null
     *
     *
     * 使用 LocalDateTime 代替 Date
     *
     * @param index 下标
     * @return LocalDateTime
     */
    fun getDateTime(index: Int): LocalDateTime {
        val value = arrayNode[index]
        return OBJECT_MAPPER.convertValue(value, LocalDateTime::class.java)
    }

    companion object {
        /**
         * 反序列化指定函数
         *
         * @param value 反序列化数据来源
         * @return 封装的 JacksonArray 对象
         */
        @JvmStatic
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        private fun deserialization(value: List<Any>): JacksonArray {
            val arrayNode = OBJECT_MAPPER.valueToTree<ArrayNode>(value)
            return JacksonArray(arrayNode)
        }
    }
}