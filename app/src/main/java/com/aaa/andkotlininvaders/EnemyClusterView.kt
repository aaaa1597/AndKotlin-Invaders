package com.aaa.andkotlininvaders

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Range
import android.view.View
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
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

    init {
        if(rowSize < MAXROWSIZE)
            rowSize = GameSceneViewModel.LevelInfo.level + 1
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initEnemies()
        ObjectAnimator.ofFloat(this, "translationY", (-h).toFloat(), 0f).apply {
            duration = 2200
            start()
        }
    }

    private fun initEnemies() {
        enemyList.clear()
        repeat(COLUMNSIZE) { x ->
            val enemiesList = MutableList(rowSize) { y ->
                Enemy.builder(COLUMNSIZE, measuredWidth, x, y)
            }
            val range = enemiesList.getRangeX()
            enemyList.add(
                EnemyColumn(Range<Float>(range.first, range.second), enemiesList)
            )
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        translateJob.cancel()
        firingJob.cancel()
    }
}

data class EnemyColumn(val range: Range<Float> = Range(0f,0f),
                       val enemyList: List<Enemy> = listOf()) {
    fun areAnyVisible(): Boolean {
        return enemyList.any { it.isVisible }
    }
}

fun List<Enemy>.getRangeX(): Pair<Float, Float> {
    return if (size > 0) {
        val enemy = get(0)
        Pair(enemy.enemyX - enemy.hitBoxRadius, enemy.enemyX + enemy.hitBoxRadius)
    } else {
        Pair(0F, 0F)
    }
}

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
}

interface IEnemyShip {
    fun onHit(enemyLife: Int)
    fun onDraw(canvas: Canvas)
    fun translate(offset: Long)
    fun setInitialSize(boxSize: Float, positionX: Int, positionY: Int)
    fun getPositionX(): Float
    fun getPositionY(): Float
    fun hitBoxRadius(): Float
}

class CapitalShip : IEnemyShip {
    private val drawRect = RectF(0F, 0F, 0F, 0F)
    var enemyX = 0F
    var enemyY = 0F
    private var coreRadius = 0F
    private var bridgeHeight = 0F
    override fun onHit(enemyLife: Int) {
        TODO("Not yet implemented")
    }

    override fun onDraw(canvas: Canvas) {
        TODO("Not yet implemented")
    }

    override fun translate(offset: Long) {
        TODO("Not yet implemented")
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

    override fun getPositionX(): Float {
        return 10f
        TODO("Not yet implemented")
    }

    override fun getPositionY(): Float {
        return 100f
        TODO("Not yet implemented")
    }

    override fun hitBoxRadius(): Float {
        return 10f
        TODO("Not yet implemented")
    }
}

class AlienShip : IEnemyShip {
    private val drawRect = RectF(0F, 0F, 0F, 0F)
    var enemyX = 0F
    var enemyY = 0F
    private var coreRadius = 0F
    private var bridgeHeight = 0F
    override fun onHit(enemyLife: Int) {
//        TODO("Not yet implemented")
    }

    override fun onDraw(canvas: Canvas) {
//        TODO("Not yet implemented")
    }

    override fun translate(offset: Long) {
//        TODO("Not yet implemented")
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

    override fun getPositionX(): Float {
        return 10f
        TODO("Not yet implemented")
    }

    override fun getPositionY(): Float {
        return 100f
        TODO("Not yet implemented")
    }

    override fun hitBoxRadius(): Float {
        return 10f
        TODO("Not yet implemented")
    }
}

class TieFighter : IEnemyShip {
    private val drawRect = RectF(0F, 0F, 0F, 0F)
    var enemyX = 0F
    var enemyY = 0F
    private var coreRadius = 0F
    private var bridgeHeight = 0F
    override fun onHit(enemyLife: Int) {
//        TODO("Not yet implemented")
    }

    override fun onDraw(canvas: Canvas) {
//        TODO("Not yet implemented")
    }

    override fun translate(offset: Long) {
//        TODO("Not yet implemented")
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

    override fun getPositionX(): Float {
//        TODO("Not yet implemented")
        return 10f
    }

    override fun getPositionY(): Float {
//        TODO("Not yet implemented")
        return 100f
    }

    override fun hitBoxRadius(): Float {
//        TODO("Not yet implemented")
        return 10f
    }
}