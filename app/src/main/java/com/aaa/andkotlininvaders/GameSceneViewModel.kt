package com.aaa.andkotlininvaders

import android.graphics.Canvas
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID
import kotlin.concurrent.withLock

class GameSceneViewModel : ViewModel() {
    /* 全初期化 */
    fun init() {
        BulletRemain.init()
        _scoreFlow.value = 0
        LifeGaugeInfo.init()
        SpaceShipViewInfo.init()
        BulletInfo.init()
    }

    /* レベル */
    object LevelInfo {
        var level = 0
        fun resetLevel() { level = 1 }
        fun increment() = level++
    }

    /* 残り弾薬数 */
    object BulletRemain {
        const val MAX_REMAIN = 80
        private val _remainFlow =  MutableStateFlow(0)
        val remainFlow: StateFlow<Int> = _remainFlow
        fun decrement() { _remainFlow.value-- }
        fun isRemainRed(): Boolean = ((remainFlow.value.toFloat()/MAX_REMAIN) < 0.25)
        fun init() { _remainFlow.value = MAX_REMAIN }
    }

    /* 得点 */
    private val _scoreFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    val scoreFlow: StateFlow<Int> = _scoreFlow

    /* HP情報 */
    object LifeGaugeInfo {
        const val MAX_LIFEGAUGE = 20
        private val _playerLife = MutableStateFlow(MAX_LIFEGAUGE)
        fun init() {_playerLife.value = MAX_LIFEGAUGE}
        fun getPlayerLifeFlow(): Flow<Int> = _playerLife
        fun getPlayerLifeValue() = _playerLife.value
        fun onHit() { _playerLife.value -= 2 }
    }

    /* 自機View情報 */
    object SpaceShipViewInfo {
        private val _xPos = MutableStateFlow(600f)
        val xPos: StateFlow<Float> = _xPos
        fun init() { _xPos.value = 600f }
        fun setXPos(x: Float) { _xPos.value = x}
    }

    /* 弾丸情報 */
    object BulletInfo {
        var bulletViewHeight: Int = 0
        /* bulletListへの操作はすべてlock(排他制御)してから使う */
        private val lock = java.util.concurrent.locks.ReentrantLock()
        private val bulletList = mutableListOf<Bullet>()
        fun init() { lock.withLock { bulletList.clear() } }
        fun removeallBullets() { lock.withLock { bulletList.clear() } }
        fun drawBullets(canvas: Canvas) {
            lock.withLock {
                bulletList.forEachSafe {
                    bullet,_ -> bullet.drawBullet(canvas)
                }
            }
        }
        fun cleanupBullets(measuredHeight: Int) {
            lock.withLock {
                bulletList.forEachMutableSafe { bullet, iterator ->
                    if (bullet.bulletY < 0 && bullet.bulletY > measuredHeight) {
                        iterator.remove()
                    }
                }
            }
        }
        fun findBullet(id: UUID): Bullet? {
            lock.withLock {
                return bulletList.find { it.id == id }
            }
        }
        fun removeAllBullets(id: UUID) {
            lock.withLock {
                bulletList.removeAll { it.id == id }
            }
        }
        fun addBullet(bullet: Bullet) {
            lock.withLock {
                bulletList.add(bullet)
            }
        }
    }
}