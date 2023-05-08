package com.example.objectdetectionexample.Util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View

class Draw(context: Context?, val rect: Rect, val text: String): View(context){

    private lateinit var boundaryPaint: Paint
    private lateinit var textPaint: Paint

    init {
        init()
    }

    private fun init(){
        boundaryPaint = Paint()
        boundaryPaint.color = Color.WHITE
        boundaryPaint.strokeWidth = 20f
        boundaryPaint.style = Paint.Style.STROKE

        textPaint = Paint()
        textPaint.color = Color.WHITE
        textPaint.textSize = 80f
        textPaint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawText(text, rect.centerX().toFloat(), rect.exactCenterY().toFloat(), textPaint)
        canvas?.drawRect(rect.left.toFloat()+10, rect.top.toFloat()+10,rect.right.toFloat()+10, rect.bottom.toFloat()+10, boundaryPaint)
    }

}