package cn.framework.common.http

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class MessageType(private val type: String, val message: String) {
    /**
     * 警告消息.
     */
    WARNING("WARNING", "操作警告"),

    /**
     * 正常消息.
     */
    SUCCESS("SUCCESS", "操作成功"),

    /**
     * 失败消息.
     */
    ERROR("ERROR", "操作失败");

    companion object {
        private val MESSAGE_TYPE: MutableMap<String, MessageType> = HashMap()

        init {
            values().forEach {
                MessageType.MESSAGE_TYPE[it.type] = it
            }
        }

        @JsonCreator
        fun valueFor(value: String): MessageType? {
            return MessageType.MESSAGE_TYPE[value]
        }
    }

    @JsonValue
    open fun getValue(): String {
        return type
    }
}