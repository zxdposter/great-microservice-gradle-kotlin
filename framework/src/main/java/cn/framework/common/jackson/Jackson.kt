package cn.framework.common.jackson

import cn.framework.config.jackson.JacksonConfig
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.io.IOException

fun Any.toObject() = Jackson.convertObject(this)
fun Any.toArray() = Jackson.convertArray(this)
fun Any.jsonString() = Jackson.objectToString(this)
inline fun <reified T> Any.convert() = Jackson.convert(this, object : TypeReference<T>() {})

/**
 * 对 jackson 的封装
 * 此类有两种作用
 * 1、提供了一些基础的方法
 * 2、对公用的 ObjectMapper 做统一的配置，配合 spring 自定义配置 jackson，尽量做到与 spring 框架中的 jackson 表现相同
 *
 * @author zxd
 */
abstract class Jackson {
    /**
     * 转化成 json string
     *
     * @return json string
     */
    @Throws(IOException::class)
    fun toJsonString(): String {
        return OBJECT_MAPPER.writeValueAsString(this)
    }

    /**
     * 转化成 json 对象
     *
     * @param type 对象类型
     * @return json 对象
     */
    fun <T> toJava(type: Class<T>): T? {
        return OBJECT_MAPPER.convertValue(this, type)
    }

    /**
     * 转化成 json 对象
     *
     * @param typeReference 对象类型
     * @return java 对象
     */
    fun <T> toJava(typeReference: TypeReference<T>): T {
        return OBJECT_MAPPER.convertValue(this, typeReference)
    }

    /**
     * 转化成 json 对象
     *
     * @param javaType 对象类型
     * @return json 对象
     */
    fun <T> toJava(javaType: JavaType): T? {
        return OBJECT_MAPPER.convertValue(this, javaType)
    }

    /**
     * 复写 toString，默认输出 json string
     *
     * @return json string
     */
    override fun toString(): String {
        return toJsonString()
    }

    companion object {
        /**
         * 共用对象，与系统统一设置结合，在 spring 项目中，使用 [Jackson2ObjectMapperBuilder.build()] 生成.
         */
        @JvmStatic
        var OBJECT_MAPPER: ObjectMapper = JacksonConfig.objectMapperBuilder.build()

        fun setObjectMapper(objectMapper: ObjectMapper) {
            OBJECT_MAPPER = objectMapper
        }

        fun createArrayNode(): ArrayNode {
            return OBJECT_MAPPER.createArrayNode()
        }

        fun createObjectNode(): ObjectNode {
            return OBJECT_MAPPER.createObjectNode()
        }

        /**
         * java 对象转化成封装的 JacksonObject 对象
         *
         * @param value java 对象，不能传递 String 或其它一些基础的变量
         * @return 封装的 JacksonObject 对象
         */
        fun convertObject(value: Any): JacksonObject {
            return if (value is String) {
                try {
                    OBJECT_MAPPER.readValue(value.toString(), JacksonObject::class.java)
                } catch (e: JsonProcessingException) {
                    throw RuntimeException(e)
                }
            } else OBJECT_MAPPER.convertValue(value, JacksonObject::class.java)
        }

        /**
         * java 对象转化成封装的 JacksonArray 对象
         *
         * @param value java 对象，不能传递 String 或其它一些基础的变量
         * @return 封装的 JacksonArray 对象
         */
        fun convertArray(value: Any): JacksonArray {
            return OBJECT_MAPPER.convertValue(value, JacksonArray::class.java)
        }

        /**
         * java 对象转化.
         *
         * @param value java 对象
         * @param type  转化类型
         * @return 转化后对象
         */
        fun <T> convert(value: Any, type: Class<T>): T {
            return OBJECT_MAPPER.convertValue(value, type)
        }

        /**
         * java 对象转化
         *
         * @param value         java 对象
         * @param typeReference 能够嵌套模版转化，比如 new TypeReference< Map< String,String>>(){}
         * @return 转化后对象
         */
        fun <T> convert(value: Any, typeReference: TypeReference<T>): T {
            if (value is String) {
                return OBJECT_MAPPER.readValue(value, typeReference)
            }
            return OBJECT_MAPPER.convertValue(value, typeReference)
        }

        /**
         * java 对象转化
         *
         * @param value         java 对象
         * @return 转化后对象
         */
        fun <T> convert(value: Any, javaType: JavaType): T {
            return OBJECT_MAPPER.convertValue(value, javaType)
        }

        /**
         * java 对象转化成 json string
         *
         * @param object java 对象
         * @return json string
         */
        @Throws(IOException::class)
        fun objectToString(`object`: Any): String {
            return OBJECT_MAPPER.writeValueAsString(`object`)
        }

        /**
         * json string 转化成封装 JacksonObject 对象
         *
         * @param text json string
         * @return 封装的 JacksonObject 对象
         */
        @Throws(IOException::class)
        fun parseObject(text: String): JacksonObject {
            return JacksonObject(OBJECT_MAPPER.readTree(text) as ObjectNode)
        }

        /**
         * json string 转化成 java 对象
         *
         * @param text json string
         * @return java 对象
         */
        @Throws(IOException::class)
        fun <T> parseJavaObject(text: String, typeReference: TypeReference<T>): T {
            return OBJECT_MAPPER.readValue(text, typeReference)
        }

        /**
         * json string 转化成 java 对象
         *
         * @param text json string
         * @return java 对象
         */
        @Throws(IOException::class)
        fun parseJavaObject(text: String, javaType: JavaType): Any? {
            return OBJECT_MAPPER.readValue(text, javaType)
        }

        /**
         * json string 转化成 java 对象
         *
         * @param text json string
         * @return java 对象
         */
        @Throws(IOException::class)
        fun <T> parseJavaObject(text: String, type: Class<T>): T? {
            return OBJECT_MAPPER.readValue(text, type)
        }

        /**
         * json string 转化成封装 parseArray 对象
         *
         * @param text json string
         * @return 封装的 parseArray 对象
         */
        @Throws(IOException::class)
        fun parseArray(text: String): JacksonArray {
            return JacksonArray(OBJECT_MAPPER.readTree(text) as ArrayNode)
        }

        /**
         * json string 转化成 byte 数组
         *
         * @param object java 对象
         * @return byte 数组
         */
        @Throws(IOException::class)
        fun objectToBytes(`object`: Any): ByteArray {
            return OBJECT_MAPPER.writeValueAsBytes(`object`)
        }
    }
}