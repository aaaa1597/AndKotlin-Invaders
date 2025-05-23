package com.aaa.andkotlininvaders

import android.graphics.Path
import kotlin.math.abs

class LogoPathHandler(
    val measuredWidth: Float,
    val measuredHeight: Float,
    var initialPointX: Float,
    var initialPointY: Float,
    val pathLength: Float,
) {

    var drawPath = Path()

    fun startDrawingPath(invalidate: (Path) -> Unit) {
        drawPath.reset()
        drawPath.moveTo(initialPointX, initialPointY)
        drawPath(drawPath, initialPointX, initialPointY, pathLength)
        translateAhead()
        invalidate(drawPath)
    }

    private fun drawPath(drawPath: Path, startX: Float, startY: Float, drawLength: Float) {
        val direction = getDirectionForPath(startX, startY)
        val maxLength = getMaxLength(direction)
        when (direction) {
            LogoTextView.Direction.Right -> {
                if (startX + drawLength > maxLength) {
                    val newDrawLength = maxLength - startX
                    drawPath.lineTo(maxLength, startY)
                    drawPath(drawPath, maxLength, startY, drawLength - newDrawLength)
                } else {
                    drawPath.lineTo(startX + drawLength, startY)
                }

            }
            LogoTextView.Direction.Down -> {
                if (startY + drawLength > maxLength) {
                    val newDrawLength = maxLength - startY
                    drawPath.lineTo(startX, maxLength)
                    drawPath(drawPath, startX, maxLength, drawLength - newDrawLength)
                } else {
                    drawPath.lineTo(startX, startY + drawLength)
                }
            }
            LogoTextView.Direction.Left -> {
                if (startX - drawLength < maxLength) {
                    val newLength = abs(startX - drawLength)
                    drawPath.lineTo(maxLength, startY)
                    drawPath(drawPath, maxLength, startY, newLength)
                } else {
                    drawPath.lineTo(startX - drawLength, startY)
                }
            }
            LogoTextView.Direction.UP -> {
                if (startY - drawLength < maxLength) {
                    val newLength = abs(startY - drawLength)
                    drawPath.lineTo(startX, maxLength)
                    drawPath(drawPath, startX, maxLength, newLength)
                } else {
                    drawPath.lineTo(startX, startY - drawLength)
                }
            }
        }
    }

    private fun getDirectionForPath(startX: Float, startY: Float): LogoTextView.Direction {
        return when {
            startX == 0F && startY == 0F -> {
                LogoTextView.Direction.Right
            }
            startX >= measuredWidth && startY >= 0F && startY < measuredHeight -> {
                LogoTextView.Direction.Down
            }
            startX > 0F && startX <= measuredWidth && startY >= measuredHeight -> {
                LogoTextView.Direction.Left
            }
            startX <= 0F && startY > 0F && startY <= measuredHeight -> {
                LogoTextView.Direction.UP
            }
            else -> {
                LogoTextView.Direction.Right
            }
        }
    }

    private fun getMaxLength(direction: LogoTextView.Direction) = when (direction) {
        LogoTextView.Direction.Right -> measuredWidth
        LogoTextView.Direction.Down -> measuredHeight
        LogoTextView.Direction.Left -> 0F
        LogoTextView.Direction.UP -> 0F
    }

    private fun translateAhead() {
        when (getDirectionForPath(initialPointX, initialPointY)) {
            LogoTextView.Direction.Right -> {
                initialPointX++
            }
            LogoTextView.Direction.Down -> {
                initialPointY++
            }
            LogoTextView.Direction.Left -> {
                initialPointX--
            }
            LogoTextView.Direction.UP -> {
                initialPointY--
            }
        }
    }

}
