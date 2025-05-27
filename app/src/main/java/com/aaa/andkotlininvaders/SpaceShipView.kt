package com.aaa.andkotlininvaders

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
import android.view.View
import java.util.UUID

class SpaceShipView: View, RigidBodyObject {
    /* Viewを継承するときのお約束 */
    constructor(context: Context) : super(context) {
        Log.d("aaaaa", "aaaaa constructor 001 aaaaaaaaaa")
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        Log.d("aaaaa", "aaaaa constructor 002 aaaaaaaaaa")
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        Log.d("aaaaa", "aaaaa constructor 003 aaaaaaaaaa")
    }

    private var halfWidth = 0F
    private var halfHeight = 0F
    private var currentShipPosition: Float = 0F
    private var streamLinedTopPoint = 0f
    private var bodyTopPoint = 0f
    private var wingWidth = 0F
    private var missileSize = 0F
    private var displayRect = Rect()
    private var mainBodyXRange = Range(0F, 0F)
    private var mainBodyYRange = Range(0F, 0F)
    private var leftWingsXRange = Range(0F, 0F)
    private var rightWingsXRange = Range(0F, 0F)
    private var wingsYRange = Range(0F, 0F)
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

    var onCollisionCallBack: OnCollisionCallBack? = null
        set(value) {
            field = value
            collisionDetector.onCollisionCallBack = value
        }

    override val collisionDetector: CollisionDetector = CollisionDetector(this)

    override fun removeSoftBodyEntry(bullet: UUID) {
        TODO("Not yet implemented")
    }

    override fun checkCollision(softBodyObjectData: SoftBodyObjectData) {
        TODO("Not yet implemented")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        Log.d("aaaaa", "aaaaa onSizeChanged() aaaaaaaaaa")
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
        canvas.drawMissile(startX, startY)

        startX = halfWidth + wingWidth
        canvas.drawMissile(startX, startY)

        startX = (halfWidth - wingWidth / 2)
        startY = (halfHeight + bodyTopPoint / 3F)
        canvas.drawMissile(startX, startY)

        startX = (halfWidth + wingWidth / 2)
        canvas.drawMissile(startX, startY)
    }

    private fun Canvas.drawMissile(startX: Float, startY: Float) {
        drawLine(startX, startY, startX, startY - missileSize, jetPaint)
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
}