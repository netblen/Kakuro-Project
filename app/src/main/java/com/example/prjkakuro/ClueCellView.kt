package com.example.prjkakuro

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

class ClueCellView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var horizontalSum: Int = 0
    private var verticalSum: Int = 0

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 20f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val linePaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 2f
    }

    private val backgroundPaint = Paint().apply {
        color = Color.parseColor("#D3D3D3")
    }

    fun setSums(vertical: Int, horizontal: Int) {
        verticalSum = vertical
        horizontalSum = horizontal
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        if (verticalSum > 0 || horizontalSum > 0) {
            canvas.drawLine(0f, 0f, width.toFloat(), height.toFloat(), linePaint)
        }

        val textBounds = Rect()

        if (verticalSum > 0) {
            val text = verticalSum.toString()
            textPaint.textSize = width / 3.5f
            textPaint.getTextBounds(text, 0, text.length, textBounds)
            canvas.drawText(text, width * 0.25f, height * 0.75f + textBounds.height() / 2f, textPaint)
        }

        if (horizontalSum > 0) {
            val text = horizontalSum.toString()
            textPaint.textSize = width / 3.5f
            textPaint.getTextBounds(text, 0, text.length, textBounds)
            canvas.drawText(text, width * 0.75f, height * 0.35f, textPaint)
        }
    }
}