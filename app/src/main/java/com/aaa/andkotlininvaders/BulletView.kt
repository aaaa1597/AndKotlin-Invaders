package com.aaa.andkotlininvaders

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import java.util.Timer
import java.util.UUID
import kotlin.concurrent.schedule

class BulletView: View {
    /* Viewを継承するときのお約束 */
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    /* BulletViewは自発的に(100msタイマで)描画更新する */
    private var disptimer: Timer = Timer()
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        disptimer.schedule(0, 100) { invalidate() }
    }
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        disptimer.cancel()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        GameSceneViewModel.BulletInfo.bulletViewHeight = h;
    }
    override fun hasOverlappingRendering(): Boolean = false

    override fun onDraw(canvas: Canvas) {
        GameSceneViewModel.BulletInfo.cleanupBullets(measuredHeight)
        GameSceneViewModel.BulletInfo.drawBullets(canvas)
    }

    fun removeBullet(id: UUID) {
        post {
            val bullet = GameSceneViewModel.BulletInfo.findBullet(id)
            if(bullet!=null)
                GameSceneViewModel.BulletInfo.removeAllBullets(bullet.id)
        }
    }
}

enum class Sender { PLAYER, ENEMY}
class Bullet(context: Context, private val bulletX: Float, initY: Float, private val sender: Sender) {
    val id: UUID = UUID.randomUUID()
    var bulletY: Float = initY
    private val bulletSize = 40F
    private val SPEED: Int = 300
    private val updatetimer: Timer = Timer()
    private val bulletPaint = Paint().apply {
        color = if (sender == Sender.PLAYER) ResourcesCompat.getColor(context.resources, R.color.bulletColor,null)
                else Color.RED
        isAntiAlias = false
        strokeWidth = 8F
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        isDither = false
    }

    init {
        /* Bulletは(BulletView同様)、自発的に(200msタイマで)位置更新する */
        updatetimer.schedule(0, 200) { translate() }
    }

    fun drawBullet(canvas: Canvas) {
        if (sender == Sender.PLAYER)
            canvas.drawLine(bulletX,bulletY-bulletSize, bulletX, bulletY, bulletPaint)
        else
            canvas.drawLine(bulletX, bulletY, bulletX,bulletY-bulletSize, bulletPaint)
    }

    fun translateBullet() {
        translate()
    }

    private fun translate() {
        if (sender == Sender.PLAYER) {
            bulletY -= SPEED
            if (bulletY < 0)
                GameSceneViewModel.BulletInfo.removeAllBullets(this.id)
        }
        else {
            bulletY += SPEED
            if (bulletY > GameSceneViewModel.BulletInfo.bulletViewHeight)
                GameSceneViewModel.BulletInfo.removeAllBullets(this.id)
        }
    }
}
