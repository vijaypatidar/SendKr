package com.vkpapps.thunder.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.view.View
import java.io.ByteArrayOutputStream

class BitmapUtils {

    fun viewToByteArray(view: View): ByteArray {
        val picBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.RGB_565)
        val canvas = Canvas(picBitmap)
        view.draw(canvas)
        val bos = ByteArrayOutputStream()
        picBitmap.compress(Bitmap.CompressFormat.PNG, 90, bos)
        return bos.toByteArray()
    }

    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, bos)
        return bos.toByteArray()
    }

    fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }
}