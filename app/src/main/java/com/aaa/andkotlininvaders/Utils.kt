package com.aaa.andkotlininvaders

import kotlinx.coroutines.flow.MutableStateFlow

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

object Utils {
    private val _seqno = MutableStateFlow(0)
    fun getSeqno(): Int {
        _seqno.value++
        return _seqno.value
    }
}
