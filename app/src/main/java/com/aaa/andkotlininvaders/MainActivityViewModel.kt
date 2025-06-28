package com.aaa.andkotlininvaders

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainActivityViewModel : ViewModel() {
    /* ミュート設定 */
    private val _mute: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val mute = _mute.asStateFlow()
    fun toggleMute() {
        _mute.value = !_mute.value
        if(_mute.value)
            Log.d("aaaaa", "aaaaa _mute.value=${_mute.value}")
        else
            Log.d("aaaaa", "aaaaa _mute.value=${_mute.value}")
    }

    /* レベル */
    object LevelInfo {
        var level = 0
            get() = field
        fun resetLevel() { level = 0 }
        fun increment() = level++
    }
}
