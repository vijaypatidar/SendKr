package com.vkpapps.sendkr.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class HorizontalProgressBar : View {
    private var progress: Float = 0f

    companion object {
        val paint: Paint = Paint().apply {
            color = Color.parseColor("#3AFC820F")
            style = Paint.Style.FILL
        }
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    private val widthOnePercent by lazy {
        width.toFloat() / 100
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, widthOnePercent * progress, height.toFloat(), paint)
    }

    fun setProgress(progress: Float) {
        this.progress = progress
        invalidate()
    }
}