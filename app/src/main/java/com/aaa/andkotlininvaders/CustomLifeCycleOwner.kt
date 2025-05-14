package com.aaa.andkotlininvaders

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.coroutineScope

class CustomLifeCycleOwner : LifecycleOwner {
    private val mLifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

    override val lifecycle: Lifecycle
        get() = mLifecycleRegistry

    val customViewLifeCycleScope: LifecycleCoroutineScope
        get() = lifecycle.coroutineScope

    fun stopListening() {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    fun startListening() {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    init {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }
}