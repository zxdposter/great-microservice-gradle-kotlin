package cn.framework.common.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


/**
 * 支持多种类型的日期字符串输入
 *
 * 还支持 long 毫秒的解析
 */
class MultipleLocalDateTimeSerializer(defaultPattern: String, otherPattern: Array<String>? = null) :
    StdDeserializer<LocalDateTime>(LocalDateTime::class.java) {

    private val defaultPattern: DateTimeFormatter
    private val otherPattern: List<DateTimeFormatter>?

    init {
        this.defaultPattern = DateTimeFormatter.ofPattern(defaultPattern)
        this.otherPattern = otherPattern?.map {
            DateTimeFormatter.ofPattern(it)
        }
    }

    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): LocalDateTime {
        val value = jp.readValueAs(String::class.java)
        return deserialize(value)
    }

    fun deserialize(value:String): LocalDateTime {
        try {
            // 使用默认格式解析
            return LocalDateTime.parse(value, defaultPattern)
        } catch (e: Exception) {
            // 如果失败使用其它格式解析
            otherPattern?.firstNotNullOfOrNull {
                try {
                    LocalDateTime.parse(value, it)
                } catch (e: Exception) {
                    null
                }
            }?.let {
                return it
            } ?: run {
                // 其它全部格式解析失败时，判断值是否为 Long
                value.toLongOrNull()?.let {
                    return LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
                } ?: throw e
            }
        }
    }
}
