package cn.framework.common.http

import cn.framework.common.jackson.Jackson
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import org.slf4j.helpers.MessageFormatter
import java.time.LocalDateTime

/**
 * http 消息统一格式.
 *
 * @author zxd
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class RequestResult {
    /**
     * 信息类型，默认为成功
     */
    var type = MessageType.SUCCESS
        private set

    /**
     * 不展示信息，内部错误，前端 console 打印.
     */
    var reason: String? = null
        private set

    /**
     * 用于与前端交互，在较为复杂的情况下，改变行为，没有可不设置，使用 type 代替就可以.
     */
    var code: Int? = null
        private set

    /**
     * 变量生成时间，用于校对前后端消息发起和接收的时间.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    var time = LocalDateTime.now()!!
        private set

    var value: Any? = null
        private set

    /**
     * 页面弹出展示，用于展示给使用用户.
     */
    var message: String? = null
        get() = field ?: type.message
        private set

    fun setType(type: MessageType): RequestResult {
        this.type = type
        return this
    }

    fun setMessage(message: String?): RequestResult {
        this.message = message
        return this
    }

    fun setReason(reason: String?): RequestResult {
        this.reason = reason
        return this
    }

    fun setCode(code: Int?): RequestResult {
        this.code = code
        return this
    }

    fun setTime(time: LocalDateTime): RequestResult {
        this.time = time
        return this
    }

    fun setValue(value: Any?): RequestResult {
        this.value = value
        return this
    }

    fun setMessage(message: String, vararg args: Any?): RequestResult {
        this.message = MessageFormatter.arrayFormat(message, args).message
        return this
    }

    fun setReason(message: String, vararg args: Any?): RequestResult {
        reason = MessageFormatter.arrayFormat(message, args).message
        return this
    }

    fun toJsonBytes(): ByteArray {
        return Jackson.objectToBytes(this)
    }

    fun errorThr():RequestResult {
        (type == MessageType.ERROR).ifThr(this)
        return this
    }

    @get:JsonIgnore
    val isSuccess: Boolean
        get() = type == MessageType.SUCCESS

    companion object {
        fun success(): RequestResult {
            return RequestResult().setType(MessageType.SUCCESS)
        }

        fun success(message: String?, vararg args: Any?): RequestResult {
            return success().setMessage(MessageFormatter.arrayFormat(message, args).message)
        }

        fun warning(): RequestResult {
            return RequestResult().setType(MessageType.WARNING)
        }

        fun warning(message: String?, vararg args: Any?): RequestResult {
            return warning().setMessage(MessageFormatter.arrayFormat(message, args).message)
        }

        fun error(): RequestResult {
            return RequestResult().setType(MessageType.ERROR)
        }

        fun error(message: String?, vararg args: Any?): RequestResult {
            return error().setMessage(MessageFormatter.arrayFormat(message, args).message)
        }
    }
}