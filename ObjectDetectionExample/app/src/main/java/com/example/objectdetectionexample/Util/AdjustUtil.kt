package com.example.objectdetectionexample.Util

import android.graphics.*
import android.util.Log

object AdjustUtil {
    private const val TAG = "AdjustUtil"
    fun getConvertedValue(f: Float): Float {
        return f * 0.1f
    }

    fun setBrightness(i: Int): PorterDuffColorFilter {
        return if (i >= 100) {
            PorterDuffColorFilter(
                Color.argb(
                    (i - 100) * 255 / 100,
                    255,
                    255,
                    255
                ), PorterDuff.Mode.SRC_OVER
            )
        } else PorterDuffColorFilter(
            Color.argb(
                (100 - i) * 255 / 100,
                0,
                0,
                0
            ), PorterDuff.Mode.SRC_ATOP
        )
    }

    fun changeBitmapSaturation(f: Float, f2: Float, f3: Float, bitmap: Bitmap): Bitmap? {
        return try {
            val createBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
            val canvas = Canvas(createBitmap)
            val paint = Paint()
            val colorMatrix = ColorMatrix()
            colorMatrix.set(
                floatArrayOf(
                    f,
                    0.0f,
                    0.0f,
                    0.0f,
                    f2,
                    0.0f,
                    f,
                    0.0f,
                    0.0f,
                    f2,
                    0.0f,
                    0.0f,
                    f,
                    0.0f,
                    f2,
                    0.0f,
                    0.0f,
                    0.0f,
                    1.0f,
                    0.0f
                )
            )
            val colorMatrix2 = ColorMatrix()
            colorMatrix2.setSaturation(f3)
            colorMatrix.postConcat(colorMatrix2)
            paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
            canvas.drawBitmap(bitmap, Matrix(), paint)
            createBitmap
        } catch (e: Exception) {
            Log.e(TAG, "changeBitmapSaturation: $e")
            null
        }
    }

    fun changeBitmapContrastBrightness(bitmap: Bitmap, f: Float, f2: Float): Bitmap {
        val colorMatrix = ColorMatrix(
            floatArrayOf(
                f,
                0.0f,
                0.0f,
                0.0f,
                f2,
                0.0f,
                f,
                0.0f,
                0.0f,
                f2,
                0.0f,
                0.0f,
                f,
                0.0f,
                f2,
                0.0f,
                0.0f,
                0.0f,
                1.0f,
                0.0f
            )
        )
        val createBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        val canvas = Canvas(createBitmap)
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint)
        return createBitmap
    }
}