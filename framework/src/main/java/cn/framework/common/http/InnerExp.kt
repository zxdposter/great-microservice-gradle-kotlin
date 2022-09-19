package cn.framework.common.http

import org.slf4j.helpers.MessageFormatter
import java.util.function.Supplier

/*
扩展 Boolean 的方法，能够使用直接根据布尔类型值抛出异常
例如 list.isEmpty().ifThr，当 list 为空时，就能够抛出异常
 */
fun Boolean.ifThr(message: String?, vararg args: Any?) = InnerExp.ifThr(this, message, args)
fun Boolean.ifThr(requestResult: RequestResult) = InnerExp.ifThr(this, requestResult)
fun Boolean.ifThr(exception: Exception?, message: String?, vararg args: Any?) =
    InnerExp.ifThr(this, exception, message, args)

fun Boolean.ifThr(exception: Exception?, requestResult: RequestResult) =
    InnerExp.ifThr(this, exception, requestResult)

fun Boolean.ifThr(supplier: Supplier<String?>, vararg args: Any?) = InnerExp.ifThr(this, supplier, args)
fun Boolean.ifThr(supplier: Supplier<RequestResult>) = InnerExp.ifThr(this, supplier)
fun Boolean.ifThr(exception: Exception?, supplier: Supplier<String?>, vararg args: Any?) =
    InnerExp.ifThr(this, exception, supplier, args)

fun Boolean.ifThr(exception: Exception?, supplier: Supplier<RequestResult>) =
    InnerExp.ifThr(this, exception, supplier)

fun Boolean.falseThr(message: String?, vararg args: Any?) = InnerExp.ifThr(!this, message, args)
fun Boolean.falseThr(requestResult: RequestResult) = InnerExp.ifThr(!this, requestResult)
fun Boolean.falseThr(exception: Exception?, message: String?, vararg args: Any?) =
    InnerExp.ifThr(!this, exception, message, args)

fun Boolean.falseThr(exception: Exception?, requestResult: RequestResult) =
    InnerExp.ifThr(!this, exception, requestResult)

fun Boolean.falseThr(supplier: Supplier<String?>, vararg args: Any?) = InnerExp.ifThr(!this, supplier, args)
fun Boolean.falseThr(supplier: Supplier<RequestResult>) = InnerExp.ifThr(!this, supplier)
fun Boolean.falseThr(exception: Exception?, supplier: Supplier<String?>, vararg args: Any?) =
    InnerExp.ifThr(!this, exception, supplier, args)

fun Boolean.falseThr(exception: Exception?, supplier: Supplier<RequestResult>) =
    InnerExp.ifThr(!this, exception, supplier)

/**
 * 内部异常，用于在嵌套较深的情况下，直接返回数据.
 *
 * @author zxd
 */
class InnerExp : RuntimeException, Supplier<InnerExp> {
    val result: RequestResult

    constructor(result: RequestResult) : super(result.message) {
        this.result = result
    }

    constructor(msg: String?, vararg args: Any?) : super(msg) {
        result = RequestResult.error(msg, *args)
    }

    constructor(result: RequestResult, exception: Exception?) : super(result.message, exception) {
        this.result = result
    }

    constructor(msg: String?, exception: Exception?) : super(msg, exception) {
        result = RequestResult.error(msg)
    }

    override fun get(): InnerExp {
        return this
    }

    companion object {
        fun ifThr(b: Boolean, message: String?, vararg args: Any?) {
            if (b) {
                throw InnerExp(MessageFormatter.arrayFormat(message, args).message)
            }
        }

        fun ifThr(b: Boolean, requestResult: RequestResult) {
            if (b) {
                throw InnerExp(requestResult)
            }
        }

        fun ifThr(b: Boolean, exception: Exception?, message: String?, vararg args: Any?) {
            if (b) {
                throw InnerExp(
                    MessageFormatter.arrayFormat(message, args).message,
                    exception
                )
            }
        }

        fun ifThr(b: Boolean, exception: Exception?, requestResult: RequestResult) {
            if (b) {
                throw InnerExp(requestResult, exception)
            }
        }

        fun ifThr(b: Boolean, supplier: Supplier<String?>, vararg args: Any?) {
            if (b) {
                val message = supplier.get()
                throw InnerExp(MessageFormatter.arrayFormat(message, args).message)
            }
        }

        fun ifThr(b: Boolean, supplier: Supplier<RequestResult>) {
            if (b) {
                val requestResult = supplier.get()
                throw InnerExp(requestResult)
            }
        }

        fun ifThr(b: Boolean, exception: Exception?, supplier: Supplier<String?>, vararg args: Any?) {
            if (b) {
                val message = supplier.get()
                throw InnerExp(
                    MessageFormatter.arrayFormat(message, args).message,
                    exception
                )
            }
        }

        fun ifThr(b: Boolean, exception: Exception?, supplier: Supplier<RequestResult>) {
            if (b) {
                val requestResult = supplier.get()
                throw InnerExp(requestResult, exception)
            }
        }

        @Throws(InnerExp::class)
        fun thr(message: String?, exception: Exception?, vararg args: Any?) {
            throw InnerExp(MessageFormatter.arrayFormat(message, args).message, exception)
        }

        fun thr(requestResult: RequestResult, exception: Exception?) {
            throw InnerExp(requestResult, exception)
        }

        fun thr(message: String?, vararg args: Any?) {
            throw InnerExp(MessageFormatter.arrayFormat(message, args).message)
        }

        fun thr(requestResult: RequestResult) {
            throw InnerExp(requestResult)
        }

        fun resultThr(transform: (RequestResult) -> Unit): InnerExp {
            val innerExp = InnerExp(RequestResult.error())
            transform(innerExp.result)
            return innerExp
        }
    }
}