package com.aaa.andkotlininvaders

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Picture
import android.graphics.Rect
import android.graphics.drawable.PictureDrawable
import android.util.AttributeSet
import android.util.Log
import android.util.Range
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.UUID
import kotlin.concurrent.schedule
import kotlin.math.roundToInt

class SpaceShipView: View {
    /* Viewを継承するときのお約束 */
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var bursttimer: Timer? = null
    private var halfWidth = 0F
    private var halfHeight = 0F
    private var currentShipPosition: Float = 0F
    private var streamLinedTopPoint = 0f
    private var bodyTopPoint = 0f
    private var wingWidth = 0F
    private var missileSize = 0F
    private fun getShipX() = currentShipPosition
    private fun getShipY(): Float = bodyTopPoint+y
    private var displayRect = Rect()
    private var mainBodyXRange = Range(0F, 0F)
    private var mainBodyYRange = Range(0F, 0F)
    private var leftWingsXRange = Range(0F, 0F)
    private var rightWingsXRange = Range(0F, 0F)
    private var wingsYRange = Range(0F, 0F)
    private var spaceShipxPosJob: Job = Job()
    private var collisionCheckJob: Job = Job()
    private lateinit var spaceShipPicture: Picture
    private lateinit var pictureDrawable: PictureDrawable
    private val jetPaint = Paint().apply {
        color = Color.parseColor("#F24423")
        isAntiAlias = false
        strokeWidth = 8F
        isDither = false
        setShadowLayer(10F, 0F, 10F, Color.MAGENTA)
    }
    private val bodyPaint = Paint().apply {
        color = Color.parseColor("#DEDEDE")
        isAntiAlias = false
        isDither = false
    }
    private val bodyStrokePaint = Paint().apply {
        color = Color.parseColor("#DEDEDE")
        style = Paint.Style.STROKE
        isAntiAlias = false
        isDither = false
    }
    private val wingsPaintOutline = Paint().apply {
        color = Color.parseColor("#0069DE")
        style = Paint.Style.STROKE
        strokeWidth = 2F
        isAntiAlias = false
        isDither = false
    }

    /* ワーニング対応 onTouchEvent()をoverrideしたら、performClick()を呼ばんといかんらしい　*/
    override fun performClick(): Boolean = super.performClick()
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        /* タッチイベント定義 */
        setOnTouchListener { _, motionEvent ->
            when(motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    bursttimer?.cancel()    /* まれにUPが来ずにDOWNが来ることへの対策 */
                    bursttimer = Timer().apply { schedule(0, 200) {
                        if(GameSceneViewModel.BulletRemain.remainFlow.value > 0) {
                            GameSceneViewModel.BulletRemain.decrement()
                            fire()
                        }
                    }}
                    setBackgroundColor(ContextCompat.getColor(context, R.color.burstFireOnColor))
                    GameSceneViewModel.SpaceShipViewInfo.setXPos(motionEvent.x)
                    return@setOnTouchListener true
                }
                MotionEvent.ACTION_MOVE -> {
                    GameSceneViewModel.SpaceShipViewInfo.setXPos(motionEvent.x)
                    return@setOnTouchListener true
                }
                MotionEvent.ACTION_UP -> {
                    bursttimer?.cancel()
                    setBackgroundColor(ContextCompat.getColor(context, R.color.burstFireOffColor))
                    GameSceneViewModel.SpaceShipViewInfo.setXPos(motionEvent.x)
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener performClick()
        }

        /* 自機移動 */
        spaceShipxPosJob.cancel()
        val lifecycleOwner = findViewTreeLifecycleOwner()!!
        spaceShipxPosJob = lifecycleOwner.lifecycleScope.launch {lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            GameSceneViewModel.SpaceShipViewInfo.xPos.collect {
                if (it > wingWidth && it < measuredWidth - wingWidth) {
                    currentShipPosition = it
                    mainBodyXRange  = Range(currentShipPosition-24, currentShipPosition+24)
                    leftWingsXRange = Range(currentShipPosition-wingWidth, mainBodyXRange.lower)
                    rightWingsXRange= Range(mainBodyXRange.upper, currentShipPosition+wingWidth)
                    displayRect.set((it-halfWidth).roundToInt(),0,
                                    (it+halfWidth).roundToInt(), measuredHeight)
                    invalidate()
                }
            }
        }}

        /* (要求に応じて)Ammoの衝突判定 */
        collisionCheckJob.cancel()
        collisionCheckJob = lifecycleOwner.lifecycleScope.launch {lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            GameSceneViewModel.AmmoInfo.checkTarget.collect {
                val (uuid, ammoX, ammoY) = it

                if (mainBodyYRange.contains(ammoY) && (mainBodyXRange.contains(ammoX)) )
                    onGetAmmo(uuid)

                if (wingsYRange.contains(ammoY) && leftWingsXRange.contains(ammoX) )
                    onGetAmmo(uuid)

                if (wingsYRange.contains(ammoY) && rightWingsXRange.contains(ammoX) )
                    onGetAmmo(uuid)
            }
        }}
    }

    private fun onGetAmmo(uuid: UUID) {
        GameSceneViewModel.AmmoInfo.removeAllAmmo(uuid)
        GameSceneViewModel.Score.updateScore(20)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        spaceShipxPosJob.cancel()
        collisionCheckJob.cancel()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        halfWidth = w / 2F
        getDrawingRect(displayRect)
        halfHeight = h / 2F
        currentShipPosition = halfWidth
        streamLinedTopPoint = h / 4F
        bodyTopPoint = h / 3F
        wingWidth = w / 15F
        missileSize = h / 8F
        mainBodyYRange = Range(top + streamLinedTopPoint, (top + halfHeight + bodyTopPoint) - missileSize)
        wingsYRange = Range((top + halfHeight + bodyTopPoint) - missileSize, top + halfHeight + bodyTopPoint)
        initPicture()
        ObjectAnimator.ofFloat(this, "translationY", h.toFloat(), 0f).apply {
            duration = 1200
            start()
        }
    }

    private fun initPicture() {
        spaceShipPicture = Picture()
        pictureDrawable = PictureDrawable(spaceShipPicture)
        val canvas = spaceShipPicture.beginRecording(measuredWidth, measuredHeight)
        canvas.let {
            drawExhaust(it)
            drawStreamlinedBody(it)
            drawBody(it)
            drawMisc(it)
            drawShipWings(it)
        }
        spaceShipPicture.endRecording()
        pictureDrawable = PictureDrawable(spaceShipPicture)

        postInvalidate()
    }
    private fun drawExhaust(canvas: Canvas) {
        val topPoint = halfHeight + streamLinedTopPoint / 2
        val path = Path()
        path.moveTo(halfWidth,topPoint) // Top
        path.lineTo(halfWidth - wingWidth / 10, topPoint)
        path.lineTo(halfWidth - wingWidth / 5,halfHeight + streamLinedTopPoint)
        path.lineTo(halfWidth,measuredHeight - bodyTopPoint)
        path.moveTo(halfWidth + wingWidth / 10, topPoint) // Top
        path.lineTo(halfWidth + wingWidth / 5,halfHeight + streamLinedTopPoint)
        path.lineTo(halfWidth,measuredHeight - bodyTopPoint)
        path.close()
        canvas.drawPath(path, jetPaint)
    }
    private fun drawStreamlinedBody(it: Canvas) {
        bodyStrokePaint.strokeWidth = 10F
        it.drawLine(halfWidth, streamLinedTopPoint, halfWidth,measuredHeight - streamLinedTopPoint, bodyStrokePaint)
    }
    private fun drawBody(it: Canvas) {
        bodyStrokePaint.strokeWidth = 24F
        it.drawLine(halfWidth,bodyTopPoint,halfWidth,measuredHeight - bodyTopPoint,bodyStrokePaint)
    }
    private fun drawMisc(canvas: Canvas) {
        var startY = halfHeight + bodyTopPoint
        var startX = halfWidth - wingWidth
        canvas.drawLine(startX, startY, startX, startY - missileSize, jetPaint)

        startX = halfWidth + wingWidth
        canvas.drawLine(startX, startY, startX, startY - missileSize, jetPaint)

        startX = (halfWidth - wingWidth / 2)
        startY = (halfHeight + bodyTopPoint / 3F)
        canvas.drawLine(startX, startY, startX, startY - missileSize, jetPaint)

        startX = (halfWidth + wingWidth / 2)
        canvas.drawLine(startX, startY, startX, startY - missileSize, jetPaint)
    }
    private fun drawShipWings(canvas: Canvas) {
        val path = Path()
        path.moveTo(halfWidth, halfHeight - bodyTopPoint / 3) // Top
        path.lineTo(halfWidth - wingWidth,halfHeight + bodyTopPoint) // Left
        path.lineTo(halfWidth,halfHeight + streamLinedTopPoint / 2) // Return to mid
        path.lineTo(halfWidth + wingWidth,halfHeight + bodyTopPoint) // Right
        path.close()
        canvas.drawPath(path, bodyPaint)
        canvas.drawPath(path, wingsPaintOutline)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        pictureDrawable.bounds = displayRect
        pictureDrawable.draw(canvas)
    }

    var fireSound: SoundManager? = null
    private fun fire() {
        fireSound?.play()
        GameSceneViewModel.BulletInfo.addBullet(Bullet(getShipX(), getShipY(), Sender.PLAYER))
    }

    private fun checkCollision(id: UUID, sender: Sender, bulletX: Float, bulletY: Float) {
        /* YがSpaceShipに未達 */
        if (bulletY.roundToInt() > top) {
            Log.d("aaaaa", "aaaaa --return-- id:${id} sender:${sender} bulletY(${bulletY}) > top(${top})")
            return
        }

        Log.d("aaaaa", "aaaaa id:${id} sender:${sender} bulletY(${bulletY}) <= top(${top})")

        /* YがSpaceShipに到達, XもSpaceShip本体に衝突 */
        if (mainBodyYRange.contains(bulletY) && mainBodyXRange.contains(bulletX)) {
            Log.d("aaaaa", "->onPlayerHit() id:${id} sender:${sender} bulletY(${bulletY}) ⊂ mainBodyYRange(${mainBodyYRange}) && bulletX(${bulletX}) ⊂ mainBodyXRange(${mainBodyXRange})")
            onPlayerHit()
        }
        /* YがSpaceShipに到達, XもSpaceShip左ウイングに衝突 */
        else if (wingsYRange.contains(bulletY) && leftWingsXRange.contains(bulletX)) {
            Log.d("aaaaa", "->onPlayerHit() id:${id} sender:${sender} bulletY(${bulletY}) ⊂ wingsYRange(${wingsYRange}) && bulletX(${bulletX}) ⊂ leftWingsXRange(${leftWingsXRange})")
            onPlayerHit()
        }
        /* YがSpaceShipに到達, XもSpaceShip右ウイングに衝突 */
        else if (wingsYRange.contains(bulletY) && rightWingsXRange.contains(bulletX)) {
            Log.d("aaaaa", "->onPlayerHit() id:${id} sender:${sender} bulletY(${bulletY}) ⊂ wingsYRange(${wingsYRange}) && bulletX(${bulletX}) ⊂ rightWingsXRange(${rightWingsXRange})")
            onPlayerHit()
        }
        else {
            Log.d("aaaaa", "Noooooo!! id:${id} sender:${sender} bulletY(${bulletY}) ⊂ mainBodyYRange(${mainBodyYRange}) && bulletX(${bulletX}) ⊂ mainBodyXRange(${mainBodyXRange})")
            Log.d("aaaaa", "Noooooo!! id:${id} sender:${sender} bulletY(${bulletY}) ⊂ wingsYRange(${wingsYRange}) && bulletX(${bulletX}) ⊂ leftWingsXRange(${leftWingsXRange})")
            Log.d("aaaaa", "Noooooo!! id:${id} sender:${sender} bulletY(${bulletY}) ⊂ wingsYRange(${wingsYRange}) && bulletX(${bulletX}) ⊂ rightWingsXRange(${rightWingsXRange})")
            return; /* 衝突してない */
        }

        /* 衝突した弾丸は消去 */
        GameSceneViewModel.BulletInfo.removeAllBullets(id)
    }

    private fun onPlayerHit() {
        GameSceneViewModel.Shake.onHit()
        GameSceneViewModel.LifeGaugeInfo.onHit()
        GameSceneViewModel.Vibrator.vibrate(64, 48)
    }
}