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
import android.util.AttributeSet
import android.util.Log
import android.util.Range
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import com.aaa.andkotlininvaders.GlobalCounter.enemyTimerFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

class EnemyClusterView: View {
    /* Viewを継承するときのお約束 */
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var rowSize = 1
    private var translateJob: Job = Job()
    private var firingJob: Job = Job()
    private var gameclearJob: Job = Job()
    var fireSound: SoundManager? = null
    companion object {
        const val SPEED = 2F
        private const val MAXROWSIZE: Int = 5
        private const val COLUMNSIZE = 6
    }

    init {
        if(rowSize < MAXROWSIZE)
            rowSize = GameSceneViewModel.LevelInfo.level + 1
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        translateJob.cancel()
        firingJob.cancel()
        gameclearJob.cancel()
    }

    @OptIn(ObsoleteCoroutinesApi::class)
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initEnemies(fireSound)
        ObjectAnimator.ofFloat(this, "translationY", (-h).toFloat(), 0f).apply {
            duration = 2200
            start()
        }
        .addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(anim: Animator) {}
            override fun onAnimationCancel(anim: Animator) {}
            override fun onAnimationRepeat(anim: Animator) {}
            override fun onAnimationEnd(anim: Animator) {
                val lifecycleOwner = findViewTreeLifecycleOwner()!!
                translateJob.cancel()
                translateJob = lifecycleOwner.lifecycleScope.launch {lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    enemyTimerFlow.collect {
                        val enemyList = GameSceneViewModel.EnemyInfo.enemiesLines
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

                firingJob.cancel()
                firingJob = lifecycleOwner.lifecycleScope.launch {lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    ticker(1000, 200).receiveAsFlow().collect {
                        val enemyList = GameSceneViewModel.EnemyInfo.enemiesLines
                        if (enemyList.isNotEmpty()) {
                            val enemyLine = enemyList.random()
                            val enemy = enemyLine.enemyList.findLast { it.isVisible }
                            enemy?.onFireCanon(enemy.enemyX, enemy.enemyY)
                        }
                    }
                }}

                gameclearJob.cancel()
                gameclearJob = lifecycleOwner.lifecycleScope.launch {lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    GameSceneViewModel.EnemyInfo.enemiesEliminated.collect {
                        if(it == 0) return@collect
                        findNavController().navigate(R.id.action_to_gamecleared_zoom)
                    }
                }}
            }
        })
    }

    private fun initEnemies(fireSound: SoundManager?) {
        GameSceneViewModel.EnemyInfo.enemiesLines.clear()
        repeat(COLUMNSIZE) { x ->
            val enemiesList = MutableList(rowSize) { y ->
                                Enemy.builder(fireSound, COLUMNSIZE, measuredWidth, x, y)
                              }
            val range = enemiesList.getRangeX()
            val enemyList = GameSceneViewModel.EnemyInfo.enemiesLines
            enemyList.add(EnemyColumn(Range(range.first, range.second), enemiesList))
        }
    }

    private fun baseLost() {
        GameSceneViewModel.EnemyInfo.enemiesLines.clear()
        enemyDetailsCallback?.onEnemyBreached()
        GameSceneViewModel.Vibrator.vibrate(320)
        postInvalidate()
    }

    private fun translateEnemy(millisUntilFinished: Long) {
        val enemiesLines = GameSceneViewModel.EnemyInfo.enemiesLines
        enemiesLines.flattenedForEach { enemy ->
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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val enemiesLines = GameSceneViewModel.EnemyInfo.enemiesLines
        enemiesLines.flattenedForEach {
            it.onDraw(canvas)
        }
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

inline fun List<EnemyColumn>.checkXForEach(x: Float, checkCollision: (EnemyColumn) -> Unit) {
    val iterator = iterator()
    while (iterator.hasNext()) {
        val enemyColumn = iterator.next()
        if (enemyColumn.range.contains(x) && enemyColumn.areAnyVisible()) {
            checkCollision(enemyColumn)
            return
        }
    }
}

/************************************************/
/* EnemyClusterView ◇--- EnemyColumn ◇--- Enemy */
/************************************************/
class Enemy(private var fireSound: SoundManager?) {
    var isVisible: Boolean = true
    var enemyLife = Random.nextInt(1, 4)
    private val points = enemyLife * 25L
    private val hasDrops: Boolean = (Random.nextDouble(0.0, 1.0) > 0.8)
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

    fun onFireCanon(enemyX: Float, enemyY: Float) {
        fireSound?.play()
        GameSceneViewModel.BulletInfo.addBullet(Bullet(enemyX, enemyY, Sender.ENEMY, ::checkCollision))
    }

    private fun checkCollision(id: UUID, bulletX: Float, bulletY: Float) {
        val enemiesLines = GameSceneViewModel.EnemyInfo.enemiesLines
        enemiesLines.checkXForEach(bulletX) {            /* ここでX座標コリジョンをチェック */
            val findedEnemy = it.enemyList.reversed().find { enemy ->
                                enemy.checkEnemyYPosition(bulletY)     /* ここでY座標コリジョンをチェック */
                              }

            findedEnemy?.let { enemy ->
                destroyEnemy(enemy)
                GameSceneViewModel.BulletInfo.removeAllBullets(id)
                val anyVisible = GameSceneViewModel.EnemyInfo.enemiesLines.any {
                                    it.areAnyVisible()
                                }
                /* 敵機全排除 → クリア */
                if (!anyVisible) {
                    GameSceneViewModel.Vibrator.vibrate(320)
                    /* 完了後、ゲーム画面に遷移 */
                    GameSceneViewModel.EnemyInfo.enemiesAllEliminated()
                }
            }
        }
    }

    private fun checkEnemyYPosition(bulletY: Float): Boolean {
        return Range(enemyDelegate.getPositionY() - enemyDelegate.hitBoxRadius(),
            enemyDelegate.getPositionY() + enemyDelegate.hitBoxRadius()).contains(bulletY) && isVisible
    }

    private fun destroyEnemy(destroyEnemy: Enemy) {
        destroyEnemy.onHit()
        fireSound?.play()
        GameSceneViewModel.Vibrator.vibrate(64, 48)
        dropAmmoIfItHave(destroyEnemy)
    }

    fun onHit() {
        enemyLife--
        if (enemyLife <= 0)
            GameSceneViewModel.Score.updateScore(points)
        enemyDelegate.onHit(enemyLife)
        isVisible = enemyLife > 0
    }

    private var ammoDropsList = mutableListOf<Ammo>()
    private fun dropAmmoIfItHave(enemy: Enemy) {
        if(!enemy.hasDrops) return
        if(enemy.enemyLife != 0) return
        if(GameSceneViewModel.LevelInfo.level == 0) return
        ammoDropsList.add(Ammo(enemy.enemyX, enemy.enemyY, ::checkCollision))
    }

    fun onDraw(canvas: Canvas) {
        if (isVisible)
            enemyDelegate.onDraw(canvas)
    }

    companion object {
        fun builder(fireSound: SoundManager?, columnSize: Int, width: Int, positionX: Int, positionY: Int): Enemy {
            return Enemy(fireSound).apply {
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

    private fun drawWingsAndBody(wingWidth: Float, topHeight: Float, canvas: Canvas) {
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

    private fun drawGuns( wingWidth: Float, topHeight: Float, canvas: Canvas) {
        canvas.drawLine(enemyX - wingWidth, enemyY - topHeight, enemyX - coreRadius, enemyY - topHeight, strokePaint)
        canvas.drawLine(enemyX + wingWidth, enemyY - topHeight, enemyX + coreRadius, enemyY - topHeight,strokePaint)

        canvas.drawLine( enemyX - coreRadius, enemyY - topHeight, enemyX - coreRadius, enemyY + topHeight, strokePaint)
        canvas.drawLine( enemyX + coreRadius, enemyY - topHeight, enemyX + coreRadius, enemyY + topHeight, strokePaint)
    }

    override fun translate(offset: Long) {
        enemyY += EnemyClusterView.SPEED
        drawRect.offset(0F, EnemyClusterView.SPEED)
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
        enemyY += EnemyClusterView.SPEED
        drawRect.offset(0F, EnemyClusterView.SPEED)
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
        enemyY += EnemyClusterView.SPEED
        drawRect.offset(0F, EnemyClusterView.SPEED)
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
