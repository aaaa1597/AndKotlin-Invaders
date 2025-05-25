package com.aaa.andkotlininvaders

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aaa.andkotlininvaders.GameSceneViewModel.LifeGaugeInfo
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class LifeGaugeView: View {
    /* Viewを継承するときのお約束 */
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val heartPaint by lazy {
        Paint().apply {
            color = Color.parseColor("#DD3D1F")
        }
    }

    private val circlePaint by lazy {
        Paint().apply {
            color = ResourcesCompat.getColor(context.resources, R.color.shipShadowColor, null)
            strokeWidth = 2F
            style = Paint.Style.STROKE
        }
    }

    private val lifeProgressPaint by lazy {
        Paint().apply {
            color = Color.parseColor("#DD3D1F")
            style = Paint.Style.STROKE
            strokeWidth = measuredHeight / 4F
            if (isHardwareAccelerated)
                setShadowLayer(12F, 0F, 0F, color)
        }
    }

    /* ライフゲージが0になった時に呼ぶ関数 */
    var onLifeEmpty: (() -> Unit)? = null
    /* 現在ライフゲージ長(px) */
    var progressLength = 0F

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        progressLength = map(LifeGaugeInfo.getPlayerLifeValue(),0, LifeGaugeInfo.MAX_LIFEGAUGE
                                                                 , measuredHeight, measuredWidth)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isInEditMode)
            startCollectLile()
    }

    private fun startCollectLile() {
        val lifecycleOwner = findViewTreeLifecycleOwner()!!
        lifecycleOwner.lifecycleScope.launch {lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            GameSceneViewModel.LifeGaugeInfo.getPlayerLifeFlow().collect { life ->
                launch {
                    if(life <= 0)
                        onLifeEmpty?.invoke()
                    val progress = map(life, 0, LifeGaugeInfo.MAX_LIFEGAUGE, measuredHeight, measuredWidth)
                    animateProgress(progress)
                }
            }
        }}
    }

    private var valueAnimator: ValueAnimator? = null
    private fun animateProgress(progress: Float) {
        if (progressLength != progress) {
            valueAnimator?.cancel()

            valueAnimator = ValueAnimator.ofFloat(progressLength, progress)
                .setDuration(500L).apply {
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener {
                        val currentProgress = it.animatedValue
                        if (currentProgress is Float) {
                            progressLength = currentProgress
                            postInvalidate()
                        }
                    }
                    start()
                }
        }
    }

    override fun onDraw(canvas: Canvas) {
        val radius = measuredHeight / 2F
        canvas.drawCircle(radius + paddingLeft, radius + paddingTop, radius, circlePaint)
        canvas.drawLine(measuredHeight.toFloat(), radius, progressLength, radius, lifeProgressPaint)
        val path = createHeartPath(2 * (radius.roundToInt() + paddingLeft), measuredHeight)
        canvas.drawPath(path, heartPaint)
    }

    private fun createHeartPath(width: Int, height: Int): Path {
        val path = Path()
        val bottomPointX = width / 2F
        val bottomPointY = 0.9F * height
        val midPointLength = 0.4 * height
        val topSidePointLength = 0.7 * height

        val controlPointHeight = midPointLength * 0.6

        //start point
        path.moveTo(bottomPointX, bottomPointY)

        var angle = 225.0

        //left mid point
        val midPointLeftX = bottomPointX + midPointLength * cos(Math.toRadians(angle))
        val midPointLeftY = bottomPointY + midPointLength * sin(Math.toRadians(angle))
        path.lineTo(midPointLeftX.toFloat(), midPointLeftY.toFloat())

        angle = 220.0

        //control point left
        val controlPointLeftX =
            midPointLeftX + controlPointHeight * cos(Math.toRadians(angle))
        val controlPointLeftY =
            midPointLeftY + controlPointHeight * sin(Math.toRadians(angle))

        angle = 235.0

        //top left point
        val topLeftPointX = bottomPointX + topSidePointLength * cos(Math.toRadians(angle))
        val topLeftPointY = bottomPointY + topSidePointLength * sin(Math.toRadians(angle))

        path.quadTo(
            controlPointLeftX.toFloat(),
            controlPointLeftY.toFloat(),
            topLeftPointX.toFloat(),
            topLeftPointY.toFloat()
        )

        //top control point left

        val offsetXControlPoint = width * 0.2F
        val offsetYControlPoint = 0F

        val controlPointTopX = width / 2 - offsetXControlPoint


        //mid point top
        val midTopX = width / 2F
        val midTopY = height * 0.3F
        path.quadTo(
            controlPointTopX,
            offsetYControlPoint,
            midTopX,
            midTopY
        )
        //back to start
        path.lineTo(bottomPointX, bottomPointY)

        angle = 315.0

        //right mid point
        val midPointRightX = bottomPointX + midPointLength * cos(Math.toRadians(angle))
        val midPointRightY = bottomPointY + midPointLength * sin(Math.toRadians(angle))
        path.lineTo(midPointRightX.toFloat(), midPointRightY.toFloat())

        angle = 320.0

        //control point right
        val controlPointRightX =
            midPointRightX + controlPointHeight * cos(Math.toRadians(angle))
        val controlPointRightY =
            midPointRightY + controlPointHeight * sin(Math.toRadians(angle))

        angle = 305.0

        //top right point
        val topRightPointX = bottomPointX + topSidePointLength * cos(Math.toRadians(angle))
        val topRightPointY = bottomPointY + topSidePointLength * sin(Math.toRadians(angle))

        path.quadTo(
            controlPointRightX.toFloat(),
            controlPointRightY.toFloat(),
            topRightPointX.toFloat(),
            topRightPointY.toFloat()
        )

        //top control point right

        val controlPointTopXRight = width / 2 + offsetXControlPoint


        path.quadTo(
            controlPointTopXRight,
            offsetYControlPoint,
            midTopX,
            midTopY
        )

        return path
    }
}