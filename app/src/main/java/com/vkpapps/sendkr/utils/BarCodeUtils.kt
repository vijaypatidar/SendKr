package com.vkpapps.sendkr.utils

import android.graphics.Bitmap
import com.google.gson.GsonBuilder
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.vkpapps.sendkr.App
import com.vkpapps.sendkr.model.ConnectionBarCode
import java.io.File
import java.io.FileOutputStream


class BarCodeUtils {
    @Throws(Exception::class)
    fun createQR(data: String, path: String) {
        val matrix = QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, 1024, 1024, null)
        val pixels = IntArray(matrix.width * matrix.height)
        for (i in 0 until matrix.height) {
            val os = i * matrix.width
            for (j in 0 until matrix.width) {
                pixels[j + os] = if (matrix.get(j, i)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
            }
        }
        val bitmap = Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, matrix.width, 0, 0, matrix.width, matrix.height)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, FileOutputStream(File(path)))
    }

    fun createQR(connectionBarCode: ConnectionBarCode) {
        BarCodeUtils().createQR(GsonBuilder().create().toJson(connectionBarCode), File(StorageManager(App.context).userDir, "code.png").absolutePath)
    }
}