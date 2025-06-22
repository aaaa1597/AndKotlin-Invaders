package com.aaa.andkotlininvaders

import android.app.Application
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.os.VibrationEffect
import android.os.VibratorManager
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.Flow
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
        private val _bulletList = mutableListOf<Bullet>()
        fun init() { lock.withLock { _bulletList.clear() } }
        fun drawBullets(canvas: Canvas) {
            lock.withLock {
                _bulletList.forEach { Log.d("aaaaa", "aaaaa drawBullets() --list(${_bulletList.size})-- id:${it.id} sender(${it.sender}) bulletX(${it.bulletX}) bulletY(${it.bulletY}))") }
                _bulletList.forEachSafe {
                    bullet,_ -> bullet.drawBullet(canvas)
                }
            }
        }
        fun cleanupBullets(measuredHeight: Int) {
            lock.withLock {
                _bulletList.forEachMutableSafe { bullet, iterator ->
                    _bulletList.forEach { Log.d("aaaaa", "aaaaa cleanupBullets() --list(${_bulletList.size})-- id:${it.id} sender(${it.sender}) bulletX(${bullet.bulletX}) bulletY(${bullet.bulletY})) measuredHeight=${measuredHeight}") }
                    if (bullet.bulletY < 0 || bullet.bulletY > measuredHeight) {
                        Log.d("aaaaa", "aaaaa cleanupBullets():::deleted__b --list(${_bulletList.size})-- id:${bullet.id} sender(${bullet.sender}) bulletX(${bullet.bulletX}) bulletY(${bullet.bulletY}))")
                        iterator.remove()
                        _bulletList.forEach { Log.d("aaaaa", "aaaaa cleanupBullets():::deleted__b --list(${_bulletList.size})-- id:${it.id} sender(${it.sender}) bulletX(${bullet.bulletX}) bulletY(${bullet.bulletY}))") }
                    }
                }
            }
        }
        fun findBullet(id: UUID): Bullet? {
            lock.withLock {
                return _bulletList.find { it.id == id }
            }
        }
        fun removeAllBullets(id: UUID) {
            val stack = Throwable().stackTrace
            println("    at ${stack[0].className}.${stack[0].methodName}(${stack[0].fileName}:${stack[0].lineNumber})")
            println("    at ${stack[1].className}.${stack[1].methodName}(${stack[1].fileName}:${stack[1].lineNumber})")

            lock.withLock {
                _bulletList.forEach { Log.d("aaaaa", "aaaaa removeAllBullets()::b --list(${_bulletList.size})-- id:${it.id} sender(${it.sender}) bulletX(${it.bulletX}) bulletY(${it.bulletY}))") }
                Log.d("aaaaa", "aaaaa removeAllBullets() id:${id}")
                _bulletList.removeAll { it.id == id }
                _bulletList.forEach { Log.d("aaaaa", "aaaaa removeAllBullets()::a --list(${_bulletList.size})-- id:${it.id} sender(${it.sender}) bulletX(${it.bulletX}) bulletY(${it.bulletY}))") }
            }
        }
        fun addBullet(bullet: Bullet) {
            lock.withLock {
                _bulletList.forEach { Log.d("aaaaa", "aaaaa addBullet() --list(${_bulletList.size})-- id:${it.id} sender(${it.sender}) bulletX(${bullet.bulletX}) bulletY(${bullet.bulletY}))") }
                Log.d("aaaaa", "aaaaa addBullet() id:${bullet.id} sender:${bullet.sender} bulletX(${bullet.bulletX}) bulletY(${bullet.bulletY}))")
                _bulletList.add(bullet)
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
        private val _checkTarget = MutableStateFlow(Triple(UUID.randomUUID(), 0f, 0f))
        val checkTarget = _checkTarget.asStateFlow()
        fun addAmmo(ammo: Ammo) {
            _lock.withLock {
                ammoList.add(ammo)
            }
        }
        fun cleanupAmmos() {
            _lock.withLock {
                ammoList.forEachMutableSafe { ammo, iterator ->
                    if (ammo.ammoY < 0)
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
        fun reqCollisionCheck(id: UUID, bulletX: Float, bulletY: Float) {
            _checkTarget.value = Triple(id, bulletX, bulletY)
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
