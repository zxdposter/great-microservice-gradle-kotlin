package cn.business.foundation.common.page

/*
fun <T, R> PageResult.Companion.of(page: HitsMetadata<T>, transform: (Hit<T>) -> R): PageResult<R> {
    val arrayList = ArrayList<R>(page.hits().size)
    for (hit in page.hits()) {
        arrayList.add(transform(hit))
    }
    return PageResult(arrayList, page.total()!!.value())
}*/
