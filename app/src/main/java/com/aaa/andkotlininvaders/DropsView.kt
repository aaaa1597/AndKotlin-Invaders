package com.aaa.andkotlininvaders

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import java.util.Timer
import kotlin.concurrent.schedule

class DropsView: View {
    /* Viewを継承するときのお約束 */
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    /* DropsViewは自発的に(100msタイマで)描画更新する */
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
        GameSceneViewModel.AmmoInfo.dropViewHeight = h
    }

    override fun onDraw(canvas: Canvas) {
        GameSceneViewModel.AmmoInfo.cleanupAmmos()
        GameSceneViewModel.AmmoInfo.drawAmmo(canvas)
    }
}

class Ammo(private val ammoX: Float, shipY: Float) {
    val id: Int = Utils.getSeqno()
    var ammoY: Float = shipY
    val SPEED: Int = 100
    private val updatetimer: Timer = Timer()
    private val ammoPaint = Paint().apply {
        color = GameSceneViewModel.COLOR_AMMO_DROP
        isAntiAlias = false
        strokeWidth = 5F
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 4F
        isDither = false
    }
    private val midCirclePaint = Paint().apply {
        color = GameSceneViewModel.COLOR_AMMO_MIDCIRCLE
        isAntiAlias = false
        isDither = false
    }

    private val capsuleLength = GameSceneViewModel.AmmoInfo.dropViewHeight * 0.02F
    private val capsuleLHeight= GameSceneViewModel.AmmoInfo.dropViewHeight * 0.01F

    private val drawRect = RectF(ammoX - capsuleLength, shipY - capsuleLHeight,
                                 ammoX + capsuleLength, shipY + capsuleLHeight)

    init {
        /* Bulletは(BulletView同様)、自発的に(200msタイマで)位置更新&コリジョン判定をする */
        updatetimer.schedule(0, 200) {
            translate()
            GameSceneViewModel.AmmoInfo.reqCollisionCheck(id, ammoX, ammoY)
        }
    }

    private fun translate() {
        drawRect.top += SPEED.toFloat()
        drawRect.bottom += SPEED.toFloat()
        ammoY = drawRect.bottom
        if (ammoY > GameSceneViewModel.AmmoInfo.dropViewHeight) {
            GameSceneViewModel.AmmoInfo.removeAllAmmo(this.id)
            updatetimer.cancel()
        }
    }

    fun drawAmmo(canvas: Canvas) {
        if(ammoY < 0) return
        if(GameSceneViewModel.AmmoInfo.dropViewHeight < ammoY) return
        canvas.drawRoundRect(drawRect, capsuleLHeight, capsuleLHeight, ammoPaint)
        canvas.drawCircle(ammoX, ammoY, capsuleLHeight / 2, midCirclePaint)
    }
}
