package com.aaa.andkotlininvaders

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
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
    private val _bulletCountFlow =  MutableStateFlow(0)
    val bulletCountFlow: StateFlow<Int> = _bulletCountFlow
    /* 残弾アラート取得 */
    fun isRemainRed(): Boolean
        = ((bulletCountFlow.value.toFloat()/initBullet) < 0.25)
    /* 残弾数初期化 */
    fun setInitBullet(remain: Int) {
        Log.d("aaaaa", "aaaaa GameSceneViewModel::setInitBullet()")
        initBullet = remain
        _bulletCountFlow.value = remain
    }

    /* 得点 */
    private val _scoreFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    val scoreFlow: StateFlow<Int> = _scoreFlow

    companion object {
        const val REFILL = 80
    }

    object LifeGaugeInfo {
        const val MAX_LIFEGAUGE = 20
        private val playerLife = MutableStateFlow(MAX_LIFEGAUGE)

        fun getPlayerLifeFlow(): Flow<Int> = playerLife
        fun getPlayerLifeValue() = playerLife.value
        fun onHit() { playerLife.value -= 2 }
        fun resetHealth() { playerLife.value = 20}
    }
}