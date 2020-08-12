package com.vkpapps.thunder.utils

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File


class BarCodeUtils {
    @Throws(Exception::class)
    fun createQR(data: String, path: String) {
        val matrix = QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, 512, 512, null)
        val pixels = IntArray(matrix.width * matrix.height)
        for (i in 0 until matrix.height) {
            val os = i * matrix.width
            for (j in 0 until matrix.width) {
                pixels[j + os] = if (matrix.get(j, i)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
            }
        }
        val bitmap = Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, matrix.width, 0, 0, matrix.width, matrix.height)
        BitmapUtils.bitmapToFile(bitmap, File(path))
    }

}