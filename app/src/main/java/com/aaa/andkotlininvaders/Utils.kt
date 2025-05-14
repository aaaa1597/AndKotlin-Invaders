package com.aaa.andkotlininvaders

fun <T> List<T>.forEachSafe(operation: (T, Iterator<T>) -> Unit) {
    val iterator = iterator()
    while (iterator.hasNext()) {
        val item = iterator.next()
        operation(item, iterator)
    }
}
