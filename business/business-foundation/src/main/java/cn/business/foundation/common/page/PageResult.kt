package cn.business.foundation.common.page

import cn.framework.common.jackson.Jackson
import cn.framework.common.jackson.JacksonObject

/**
 * 统一格式返回页面查询结果
 */
class PageResult<T>(val page: List<T>, val total: Long) {
    companion object {
        fun empty(): PageResult<Any> {
            return PageResult(emptyList(), 0)
        }
    }

    fun toJson():JacksonObject {
        return Jackson.convertObject(this)
    }
}