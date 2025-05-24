package com.aaa.andkotlininvaders

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GameSceneViewModel : ViewModel() {
    /* 全初期化 */
    fun init() {
        setInitBullet(REFILL)
    }
    /* 残り弾薬数 */
    private var initBullet = 1  /* 最初に与えられる弾薬数(これが残弾アラートの基準になる) */
    private lateinit var _bulletCountFlow: MutableStateFlow<Int>
    val uiState: StateFlow<Int> = _bulletCountFlow
    /* 残弾アラート取得 */
    fun isRemainRed(): Boolean
        = ((uiState.value.toFloat()/initBullet) < 0.25)
    /* 残弾数初期化 */
    fun setInitBullet(remain: Int) {
        initBullet = remain
        _bulletCountFlow = MutableStateFlow(remain)
    }

    companion object {
        const val REFILL = 80
    }
}