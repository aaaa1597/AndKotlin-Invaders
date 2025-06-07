package com.aaa.andkotlininvaders

fun <T> List<T>.forEachSafe(operation: (T, Iterator<T>) -> Unit) {
    val iterator = iterator()
    while (iterator.hasNext()) {
        val item = iterator.next()
        operation(item, iterator)
    }
}

fun <T : Number> map(value: T, inmin: T, inmax: T, outmin: T, outmax: T,): Float {
    return (value.toFloat() - inmin.toFloat()) * (outmax.toFloat() - outmin.toFloat()) /
           (inmax.toFloat() - inmin.toFloat()) + outmin.toFloat()
}

fun <T> MutableList<T>.forEachMutableSafe(operation: (T, MutableIterator<T>) -> Unit) {
    val iterator = iterator()
    while (iterator.hasNext()) {
        val item = iterator.next()
        operation(item, iterator)
    }
}
