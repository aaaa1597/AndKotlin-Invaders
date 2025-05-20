package com.aaa.andkotlininvaders

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {
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

}
