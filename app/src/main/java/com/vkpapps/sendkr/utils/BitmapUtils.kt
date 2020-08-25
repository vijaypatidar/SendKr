package com.vkpapps.sendkr.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.view.View
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object BitmapUtils {
    private val executors: ExecutorService = Executors.newSingleThreadExecutor()

    fun viewToByteArray(view: View): ByteArray {
        val picBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.RGB_565)
        val canvas = Canvas(picBitmap)
        view.draw(canvas)
        val bos = ByteArrayOutputStream()
        picBitmap.compress(Bitmap.CompressFormat.PNG, 90, bos)
        return bos.toByteArray()
    }

    fun viewToBitmap(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, bos)
        return bos.toByteArray()
    }

    fun bitmapToFile(bitmap: Bitmap, file: File) {
        executors.submit {
            try {
                Bitmap.createScaledBitmap(bitmap, 500, bitmap.height * 500 / bitmap.width, true)
                        .compress(Bitmap.CompressFormat.PNG, 90, FileOutputStream(file))
            } catch (e: Exception) {

            }
        }
    }

    fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }
}