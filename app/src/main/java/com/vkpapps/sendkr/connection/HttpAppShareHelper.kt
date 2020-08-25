package com.vkpapps.sendkr.connection

import androidx.core.net.toFile
import com.vkpapps.sendkr.loader.PrepareAppList.sendKr
import java.io.FileInputStream
import java.io.IOException
import java.net.ServerSocket

class HttpAppShareHelper : Thread() {
    var run: Boolean = true
    override fun run() {
        super.run()
        try {
            val apk = sendKr!!.uri.toFile()
            val size: Long = apk.length()
            val serverSocket = ServerSocket(8080)
            while (run) {
                val accept = serverSocket.accept()
                val outputStream = accept.getOutputStream()
                accept.getInputStream()
                // header for download
                outputStream.write("200 OK\r\n".toByteArray())
                outputStream.write("Content-Type: application/octet-stream\r\n".toByteArray())
                outputStream.write("Content-Disposition: attachment; filename=\"SendKr.apk\"\r\n".toByteArray())
                outputStream.write("Content-Length: $size\r\n".toByteArray())
                outputStream.write("\r\n".toByteArray())

                val fis = FileInputStream(apk)
                var read: Int
                val bytes = ByteArray(2048)
                while (fis.read(bytes).apply { read = this } > 0) {
                    outputStream.write(bytes, 0, read)
                }
                outputStream.flush()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}