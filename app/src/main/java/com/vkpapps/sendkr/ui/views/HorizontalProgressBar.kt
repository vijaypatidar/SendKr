package com.vkpapps.sendkr.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class HorizontalProgressBar : View {
    private var progress: Float = 0f

    private val paint: Paint by lazy {
        val paint = Paint().apply {
            color = Color.parseColor("#3AFC820F")
            style = Paint.Style.FILL
        }
        paint
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    private val widthOnePercent by lazy {
        width.toFloat() / 100
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val i = widthOnePercent * progress
        canvas.drawRect(0f, 0f, i, height.toFloat(), paint)
    }

    fun setProgress(progress: Float) {
        this.progress = progress
        invalidate()
    }
}