package com.aaa.andkotlininvaders

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.AttributeSet
import android.util.Range
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aaa.andkotlininvaders.GlobalCounter.enemyTimerFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.random.Random

class EnemyClusterView: View {
    /* Viewを継承するときのお約束 */
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val MAXROWSIZE: Int = 5
    private val COLUMNSIZE = 6
    private var rowSize = 1
    private val enemyList = mutableListOf( EnemyColumn() )
    private var translateJob: Job = Job()
    private var firingJob: Job = Job()
    private val vibratorAT by lazy { VibratorAantenna(context) }
    companion object {
        var speed = 2F
    }

    init {
        if(rowSize < MAXROWSIZE)
            rowSize = GameSceneViewModel.LevelInfo.level + 1
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        translateJob.cancel()
        firingJob.cancel()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initEnemies()
        ObjectAnimator.ofFloat(this, "translationY", (-h).toFloat(), 0f).apply {
            duration = 2200
            start()
        }
        .addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(anim: Animator) {}
            override fun onAnimationCancel(anim: Animator) {}
            override fun onAnimationRepeat(anim: Animator) {}
            override fun onAnimationEnd(anim: Animator) {
                translateJob.cancel()
                var lifecycleOwner = findViewTreeLifecycleOwner()!!
                translateJob = lifecycleOwner.lifecycleScope.launch {lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    enemyTimerFlow.collect {
                        enemyList.checkIfYReached(measuredHeight) { breachedMax ->
                            if (breachedMax)
                                baseLost()
                            if (enemyList.isNotEmpty()) {
                                translateEnemy(System.currentTimeMillis())
                                invalidate()
                            }
                        }
                    }
                }}
            }
        })
    }

    private fun initEnemies() {
        enemyList.clear()
        repeat(COLUMNSIZE) { x ->
            val enemiesList = MutableList(rowSize) { y ->
                                Enemy.builder(COLUMNSIZE, measuredWidth, x, y)
                              }
            val range = enemiesList.getRangeX()
            enemyList.add(EnemyColumn(Range<Float>(range.first, range.second), enemiesList))
        }
    }

    private fun baseLost() {
        enemyList.clear()
        enemyDetailsCallback?.onEnemyBreached()
        vibratorAT.vibrate(320)
        postInvalidate()
    }

    private fun translateEnemy(millisUntilFinished: Long) {
        enemyList.flattenedForEach { enemy ->
            enemy.translate(millisUntilFinished)
        }
    }

    private var enemyDetailsCallback: EnemyClusterViewEventCallback? = null
    fun setOnEnemyClusterViewEventCallback(enemyDetailsCallback: EnemyClusterViewEventCallback) {
        this.enemyDetailsCallback = enemyDetailsCallback
    }
    interface EnemyClusterViewEventCallback {
        fun onEnemyBreached()
    }
}

/*************************************/
/* EnemyClusterView ◇--- EnemyColumn */
/*************************************/
data class EnemyColumn(val range: Range<Float> = Range(0f,0f),
                       val enemyList: List<Enemy> = listOf()) {
    fun areAnyVisible(): Boolean {
        return enemyList.any { it.isVisible }
    }
}
inline fun MutableList<EnemyColumn>.checkIfYReached(maxHeight: Int, transform: (Boolean) -> Unit) {
    transform(flatMap{ it.enemyList }.any{
                                            (it.enemyY + it.hitBoxRadius) > maxHeight && it.isVisible
                                          })
}
inline fun MutableList<EnemyColumn>.flattenedForEach(transform: (Enemy) -> Unit) {
    flatMap { it.enemyList }.forEach {
        transform(it)
    }
}

fun List<Enemy>.getRangeX(): Pair<Float, Float> {
    return if (isNotEmpty()) {
                val enemy = get(0)
                Pair(enemy.enemyX - enemy.hitBoxRadius, enemy.enemyX + enemy.hitBoxRadius)
            }
            else {
                Pair(0F, 0F)
            }
}

/************************************************/
/* EnemyClusterView ◇--- EnemyColumn ◇--- Enemy */
/************************************************/
class Enemy {
    var isVisible: Boolean = true
    var enemyLife = Random.nextInt(1, 4)
    val enemyDelegate: IEnemyShip by lazy {
        when (enemyLife) {
            1 -> CapitalShip()
            2 -> AlienShip()
            else -> TieFighter()
        }
    }
    val enemyX: Float
        get() = enemyDelegate.getPositionX()

    val enemyY: Float
        get() = enemyDelegate.getPositionY()

    val hitBoxRadius: Float
        get() = enemyDelegate.hitBoxRadius()

    companion object {
        fun builder(columnSize: Int, width: Int, positionX: Int, positionY: Int): Enemy {
            return Enemy().apply {
                val boxSize = width / columnSize.toFloat()
                enemyDelegate.setInitialSize(boxSize, positionX, positionY)
            }
        }
    }

    fun translate(offset: Long) =
        enemyDelegate.translate(offset)
}

/*************************************************/
/* Enemy types(CapitalShip,AlienShip,TieFighter) */
/*************************************************/
interface IEnemyShip {
    fun onHit(enemyLife: Int)
    fun onDraw(canvas: Canvas)
    fun translate(offset: Long)
    fun setInitialSize(boxSize: Float, positionX: Int, positionY: Int)
    fun getPositionX(): Float
    fun getPositionY(): Float
    fun hitBoxRadius(): Float
}

/* CapitalShip */
class CapitalShip : IEnemyShip {
    private val drawRect = RectF(0F, 0F, 0F, 0F)
    var enemyX = 0F
    var enemyY = 0F
    private var coreRadius = 0F
    private var bridgeHeight = 0F
    private val drawPath = Path()
    private val mainColor = Color.rgb(
        Random.nextInt(128, 255),
        Random.nextInt(128, 255),
        Random.nextInt(128, 255)
    )
    private val paint by lazy {
        Paint().apply {
            color = mainColor
            isAntiAlias = false
            isDither = false
        }
    }
    private val strokePaint by lazy {
        Paint().apply {
            style = Paint.Style.STROKE
            color = mainColor
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
            isAntiAlias = false
            strokeWidth = 5F
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isDither = false
        }
    }

    override fun onHit(enemyLife: Int) {
        val alpha = 70 * enemyLife
        paint.alpha = alpha
        strokePaint.alpha = alpha
    }

    override fun onDraw(canvas: Canvas) {
        drawBody(canvas)
    }

    private fun drawBody(canvas: Canvas) {
        drawPath.reset()
        val topHeight = coreRadius / 3
        val wingWidth = coreRadius / 2
        drawWingsAndBody(wingWidth, topHeight, canvas)
        drawGuns(wingWidth, topHeight, canvas)
    }

    private fun drawWingsAndBody(wingWidth: Float, topHeight: Float, canvas: Canvas,) {
        val roundPartTopPoint = enemyY - (2 * topHeight)
        drawPath.moveTo(enemyX - topHeight, roundPartTopPoint)
        drawPath.quadTo(enemyX, enemyY - coreRadius, enemyX + topHeight, roundPartTopPoint)

        //Right wing *badum tss*
        val wingTopPoint = enemyY - topHeight
        drawPath.lineTo(enemyX + (2 * topHeight), wingTopPoint)
        drawPath.lineTo(enemyX + wingWidth, enemyY)
        drawPath.lineTo(enemyX + wingWidth, enemyY)
        drawPath.lineTo(enemyX, enemyY + coreRadius)

        drawPath.lineTo(enemyX - wingWidth, enemyY)
        drawPath.lineTo(enemyX - wingWidth, enemyY)
        drawPath.lineTo(enemyX - (2 * topHeight), wingTopPoint)

        drawPath.close()

        canvas.drawPath(drawPath, paint)
    }

    private fun drawGuns( wingWidth: Float, topHeight: Float, canvas: Canvas,) {
        canvas.drawLine(enemyX - wingWidth, enemyY - topHeight, enemyX - coreRadius, enemyY - topHeight, strokePaint)
        canvas.drawLine(enemyX + wingWidth, enemyY - topHeight, enemyX + coreRadius, enemyY - topHeight,strokePaint)

        canvas.drawLine( enemyX - coreRadius, enemyY - topHeight, enemyX - coreRadius, enemyY + topHeight, strokePaint)
        canvas.drawLine( enemyX + coreRadius, enemyY - topHeight, enemyX + coreRadius, enemyY + topHeight, strokePaint)
    }

    override fun translate(offset: Long) {
        enemyY += EnemyClusterView.speed
        drawRect.offset(0F, EnemyClusterView.speed)
    }

    override fun setInitialSize(boxSize: Float, positionX: Int, positionY: Int) {
        drawRect.set(
            boxSize * positionX,
            boxSize * positionY,
            boxSize * (positionX + 1),
            boxSize * (positionY + 1),
        )
        enemyX = drawRect.centerX()
        enemyY = drawRect.centerY()
        coreRadius = drawRect.width() / 4F
        bridgeHeight = coreRadius / 6
    }

    override fun getPositionX(): Float = enemyX
    override fun getPositionY(): Float = enemyY
    override fun hitBoxRadius(): Float = coreRadius
}

/* CapitalShip */
class AlienShip : IEnemyShip {
    private val drawRect = RectF(0F, 0F, 0F, 0F)
    var enemyX = 0F
    var enemyY = 0F
    private var coreRadius = 0F
    private var bridgeHeight = 0F
    private val drawPath = Path()
    var rotationOffset = 0F
    private val mainColor = Color.rgb(
        Random.nextInt(128, 255),
        Random.nextInt(128, 255),
        Random.nextInt(128, 255)
    )
    private val paint by lazy {
        Paint().apply {
            style = Paint.Style.STROKE
            color = mainColor
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
            isAntiAlias = false
            strokeWidth = 10F
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isDither = false
        }
    }

    override fun onHit(enemyLife: Int) {
        paint.alpha = 70 * enemyLife
    }

    override fun onDraw(canvas: Canvas) {
        val bodyRadius = coreRadius / 4F
        drawPath.reset()
        drawPath.moveTo(enemyX, enemyY)
        //bottom
        drawPath.quadTo(enemyX - bodyRadius, enemyY + bodyRadius, enemyX, enemyY + (2F * bodyRadius))
        drawPath.quadTo(enemyX + bodyRadius, enemyY + bodyRadius, enemyX, enemyY + (coreRadius))

        drawPath.moveTo(enemyX, enemyY)

        //top
        drawPath.quadTo(enemyX + bodyRadius, enemyY - bodyRadius, enemyX, enemyY - (2F * bodyRadius))
        drawPath.quadTo(enemyX - bodyRadius, enemyY - bodyRadius , enemyX, enemyY - (coreRadius))

        drawPath.moveTo(enemyX, enemyY)

        //left
        drawPath.quadTo(enemyX - bodyRadius, enemyY + bodyRadius, enemyX - (2F * bodyRadius), enemyY)
        drawPath.quadTo(enemyX - bodyRadius, enemyY - bodyRadius, enemyX - ( coreRadius), enemyY)

        drawPath.moveTo(enemyX, enemyY)

        //right
        drawPath.quadTo(enemyX + bodyRadius, enemyY + bodyRadius, enemyX + (2F * bodyRadius), enemyY)
        drawPath.quadTo(enemyX + bodyRadius , enemyY - bodyRadius, enemyX + (coreRadius), enemyY)

        canvas.drawPath(drawPath, paint)
    }

    override fun translate(offset: Long) {
        enemyY += EnemyClusterView.speed
        drawRect.offset(0F, EnemyClusterView.speed)
        rotationOffset = offset % 90F
    }

    override fun setInitialSize(boxSize: Float, positionX: Int, positionY: Int) {
        drawRect.set(
            boxSize * positionX,
            boxSize * positionY,
            boxSize * (positionX + 1),
            boxSize * (positionY + 1),
        )
        enemyX = drawRect.centerX()
        enemyY = drawRect.centerY()
        coreRadius = drawRect.width() / 4F
        bridgeHeight = coreRadius / 6
    }

    override fun getPositionX(): Float = enemyX
    override fun getPositionY(): Float = enemyY
    override fun hitBoxRadius(): Float = coreRadius
}

/* CapitalShip */
class TieFighter : IEnemyShip {
    private val drawRect = RectF(0F, 0F, 0F, 0F)
    var enemyX = 0F
    var enemyY = 0F
    private var coreRadius = 0F
    private var bridgeHeight = 0F
    private val mainColor = Color.rgb(
        Random.nextInt(128, 255),
        Random.nextInt(128, 255),
        Random.nextInt(128, 255)
    )
    private val paint by lazy {
        Paint().apply {
            color = mainColor
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
            isAntiAlias = false
            isDither = false
//            setShadowLayer(14F, 0F, 0F, color)
        }
    }
    private val strokePaint by lazy {
        Paint().apply {
            style = Paint.Style.STROKE
            color = mainColor
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
            isAntiAlias = false
            strokeWidth = 2F
            isDither = false
//            setShadowLayer(14F, 0F, 0F, color)
        }
    }
    override fun onHit(enemyLife: Int) {
        paint.alpha = 70 * enemyLife
    }

    override fun onDraw(canvas: Canvas) {
        drawBridge(canvas)
        drawWings(canvas)
        canvas.drawCircle(enemyX, enemyY, coreRadius / 2F, paint)
    }
    private fun drawBridge(canvas: Canvas?) {
        val path = Path()
        path.moveTo(enemyX, enemyY - bridgeHeight)
        path.lineTo(enemyX - coreRadius, enemyY - 2)
        path.lineTo(enemyX - coreRadius, enemyY + 2)
        path.lineTo(enemyX, enemyY + bridgeHeight)
        path.lineTo(enemyX + coreRadius, enemyY + 2)
        path.lineTo(enemyX + coreRadius, enemyY - 2)
        path.close()
        canvas?.drawPath(path, paint)
    }
    private fun drawWings(canvas: Canvas?) {
        val yStart = enemyY - coreRadius
        val yEnd = enemyY + coreRadius
        canvas?.drawLine(enemyX - coreRadius, yStart, enemyX - coreRadius, yEnd, strokePaint)
        canvas?.drawLine(enemyX + coreRadius, yStart, enemyX + coreRadius, yEnd, strokePaint)
    }

    override fun translate(offset: Long) {
        enemyY += EnemyClusterView.speed
        drawRect.offset(0F, EnemyClusterView.speed)
    }

    override fun setInitialSize(boxSize: Float, positionX: Int, positionY: Int) {
        drawRect.set(
            boxSize * positionX,
            boxSize * positionY,
            boxSize * (positionX + 1),
            boxSize * (positionY + 1),
        )
        enemyX = drawRect.centerX()
        enemyY = drawRect.centerY()
        coreRadius = drawRect.width() / 4F
        bridgeHeight = coreRadius / 6
    }

    override fun getPositionX() = enemyX
    override fun getPositionY() = enemyX
    override fun hitBoxRadius() = coreRadius
}

class VibratorAantenna(context: Context) {
    private var vibrator:  Vibrator = (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    fun vibrate(time: Long, amplitude: Int = 255) {
        val effect = VibrationEffect.createOneShot(time, amplitude)
        vibrator.vibrate(effect)
    }
}