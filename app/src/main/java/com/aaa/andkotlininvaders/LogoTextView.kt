package com.aaa.andkotlininvaders

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class LogoTextView: AppCompatTextView {
    /* Viewを継承するときのお約束 */
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val borderPaint by lazy {
        Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 10F
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isDither = false
            isAntiAlias = false
            color = Color.parseColor("#E4962B")
            if (isHardwareAccelerated)
                setShadowLayer(12F, 0F, 0F, color)
        }
    }

    private val logoPathHandlerList: MutableList<LogoPathHandler> = mutableListOf()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        logoPathHandlerList.clear()
        logoPathHandlerList.add(LogoPathHandler(w.toFloat(), h.toFloat(), 0F, 0F, w.toFloat()))
        logoPathHandlerList.add(LogoPathHandler(w.toFloat(),
            h.toFloat(),
            w.toFloat(),
            h.toFloat(),
            w.toFloat()))
        if (h != 0)
            borderPaint.pathEffect = CornerPathEffect(12f)
    }

    enum class Direction {
        Right,
        Down,
        Left,
        UP
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isInEditMode)
            return
        canvas.let {
            logoPathHandlerList.forEach {
                it.startDrawingPath { path ->
                    canvas.drawPath(path, borderPaint)
                }
            }
            postInvalidate()
        }
    }
}