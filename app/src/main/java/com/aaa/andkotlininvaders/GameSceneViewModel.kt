package com.aaa.andkotlininvaders

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GameSceneViewModel : ViewModel() {
    /* 全初期化 */
    fun init() {
        setInitBullet(REFILL)
        _scoreFlow.value = 0
    }
    /* 残り弾薬数 */
    private var initBullet = 1  /* 最初に与えられる弾薬数(これが残弾アラートの基準になる) */
    private lateinit var _bulletCountFlow: MutableStateFlow<Int>
    val bulletCountFlow: StateFlow<Int> = _bulletCountFlow
    /* 残弾アラート取得 */
    fun isRemainRed(): Boolean
        = ((bulletCountFlow.value.toFloat()/initBullet) < 0.25)
    /* 残弾数初期化 */
    fun setInitBullet(remain: Int) {
        initBullet = remain
        _bulletCountFlow = MutableStateFlow(remain)
    }

    /* 得点 */
    private val _scoreFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    val scoreFlow: StateFlow<Int> = _scoreFlow

    companion object {
        const val REFILL = 80
    }
}