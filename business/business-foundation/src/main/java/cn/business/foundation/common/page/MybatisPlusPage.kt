package cn.business.foundation.common.page

import com.baomidou.mybatisplus.core.metadata.OrderItem
import com.baomidou.mybatisplus.extension.plugins.pagination.Page

fun <T> PageParam.page(): Page<T> {
    val page = Page<T>(current, pageSize)
    if (orderItems != null) {
        page.orders = orderItems!!.map {
            OrderItem(it.property, it.isAscending)
        }
    }
    return page
}