package cn.business.foundation.common.page

import org.springframework.data.domain.Sort.Order

/**
 * 分页查询参数，[current] 从 1 开始，
 */
data class PageParam(
    var current: Long = 1L,
    var pageSize: Long = 10L
) {
    var orderItems: List<Order>? = null

    /**
     * 给出内存排序中的开始位置
     */
    fun from(): Long {
        return (current - 1) * pageSize
    }
}