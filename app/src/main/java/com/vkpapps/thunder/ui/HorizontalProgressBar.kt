package com.vkpapps.thunder.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class HorizontalProgressBar : View {
    private var progress = 0

    private val paint: Paint by lazy {
        val paint = Paint()
        paint.color = Color.parseColor("#ffccbc")
        paint.style = Paint.Style.FILL
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
        canvas.drawRect(0f, 0f, i.toFloat(), height.toFloat(), paint)
    }

    fun setProgress(progress: Int) {
        this.progress = progress
        invalidate()
    }
}