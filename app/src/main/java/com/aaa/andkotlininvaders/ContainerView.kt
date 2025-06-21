package com.aaa.andkotlininvaders

import android.content.Context
import android.util.AttributeSet
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aaa.andkotlininvaders.GameSceneViewModel.LifeGaugeInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.random.Random

class ContainerView: ConstraintLayout {
    /* Viewを継承するときのお約束 */
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var shakeJob: Job = Job()
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val lifecycleOwner = findViewTreeLifecycleOwner()!!
        shakeJob = lifecycleOwner.lifecycleScope.launch {lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                        GameSceneViewModel.Shake.shakeFlg.collect {
                            startShakeAnimation(50, 10, 4)
                        }
                    }}
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        shakeJob.cancel()
    }

    fun startShakeAnimation(duration: Int, offset: Int, repeatCount: Int) {
        val newOffsetFromX= Random.nextInt(-1, 1).toFloat() * offset
        val newOffsetToX  = Random.nextInt(-1, 1).toFloat() * offset
        val newOffsetFromY= Random.nextInt(-1, 1).toFloat() * offset
        val newOffsetToY  = Random.nextInt(-1, 1).toFloat() * offset
        val anim: Animation = TranslateAnimation(newOffsetFromX,newOffsetToX,
                                                 newOffsetFromY,newOffsetToY)
        anim.repeatCount = 1
        anim.repeatMode = Animation.REVERSE
        anim.duration = duration.toLong()
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                val newRepeatCount = repeatCount.dec()
                if (newRepeatCount > 0)
                    startShakeAnimation(duration, offset, newRepeatCount)
            }
        })
        startAnimation(anim)
    }
}