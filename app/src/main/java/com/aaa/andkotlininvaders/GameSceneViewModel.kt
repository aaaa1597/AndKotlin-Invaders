package com.aaa.andkotlininvaders

import android.app.Application
import android.bluetooth.BluetoothLeAudio
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.VibrationEffect
import android.os.VibratorManager
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import com.aaa.andkotlininvaders.GameSceneViewModel.BulletInfo.bulletList
import com.aaa.andkotlininvaders.GameSceneViewModel.BulletInfo.lock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import kotlin.concurrent.withLock

class GameSceneViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        var COLOR_BULLET_PLAYER:Int = 0
        var COLOR_BULLET_ENEMY :Int = 0
        var COLOR_AMMO_DROP :Int = 0
        var COLOR_AMMO_MIDCIRCLE :Int = 0
        private lateinit var vibrator: android.os.Vibrator
    }

    /* 全初期化 */
    fun init() {
        COLOR_BULLET_PLAYER = ResourcesCompat.getColor(getApplication<Application>().resources, R.color.bulletColor,null)
        COLOR_BULLET_ENEMY  = Color.RED
        COLOR_AMMO_DROP     = ResourcesCompat.getColor(getApplication<Application>().resources, R.color.primaryFontColor,null)
        COLOR_AMMO_MIDCIRCLE= ResourcesCompat.getColor(getApplication<Application>().resources, R.color.shipHighLightColor, null)
        vibrator = (getApplication<Application>().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        BulletRemain.init()
        Score.init()
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
        private const val MAX_REMAIN = 80
        private val _remainFlow =  MutableStateFlow(0)
        val remainFlow: StateFlow<Int> = _remainFlow
        fun decrement() { _remainFlow.value-- }
        fun isRemainRed(): Boolean = ((remainFlow.value.toFloat()/MAX_REMAIN) < 0.25)
        fun init() { _remainFlow.value = MAX_REMAIN }
    }

    /* 得点 */
    object Score {
        private val _scoreFlow: MutableStateFlow<Long> = MutableStateFlow(0L)
        val scoreFlow: StateFlow<Long> = _scoreFlow
        fun init() { _scoreFlow.value = 0 }
        fun updateScore(points: Long) {
            _scoreFlow.value += points
        }
    }

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
                    if (bullet.bulletY < 0 || bullet.bulletY > measuredHeight) {
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

    /* 敵機情報 */
    object EnemyInfo {
        val enemiesLines = mutableListOf( EnemyColumn() )
        private val _enemiesEliminated = MutableStateFlow(0)
        val enemiesEliminated = _enemiesEliminated.asStateFlow()
        fun enemiesAllEliminated() {
            _enemiesEliminated.value = 1
        }
    }

    object AmmoInfo {
        var dropViewHeight: Int = 0
        private val _lock = java.util.concurrent.locks.ReentrantLock()
        private val ammoList = mutableListOf<Ammo>()
        fun cleanupAmmos() {
            _lock.withLock {
                ammoList.forEachMutableSafe { ammo, iterator ->
                    if (ammo.bulletY < 0)
                        iterator.remove()
                }
            }
        }
        fun drawAmmo(canvas: Canvas) {
            _lock.withLock {
                ammoList.forEachSafe {
                        bullet,_ -> bullet.drawAmmo(canvas)
                }
            }
        }
    }

    /* 画面振動 */
    object Shake {
        private val _shakeFlg =  MutableStateFlow("")
        val shakeFlg: SharedFlow<String> = _shakeFlg.asStateFlow()
        fun onHit() { _shakeFlg.value = "痛っ!!" }
   }

    /* 振動子 */
    object Vibrator {
        fun vibrate(time: Long, amplitude: Int = 255) {
            val effect = VibrationEffect.createOneShot(time, amplitude)
            vibrator.vibrate(effect)
        }
    }
}
