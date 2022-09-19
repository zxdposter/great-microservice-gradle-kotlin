package cn.business.foundation.common.page

import com.baomidou.mybatisplus.extension.plugins.pagination.Page

fun <T, R> PageResult.Companion.of(page: Page<T>, transform: (T) -> R): PageResult<R> {
    val arrayList = ArrayList<R>(page.records.size)
    for (record in page.records) {
        arrayList.add(transform(record))
    }
    return PageResult(arrayList, page.total)
}

fun <T> PageResult.Companion.of(page: Page<T>): PageResult<T> {
    return PageResult(page.records, page.total)
}
