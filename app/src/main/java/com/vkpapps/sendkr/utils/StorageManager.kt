package com.vkpapps.sendkr.utils

import android.content.Context
import android.os.Environment
import com.vkpapps.sendkr.App.Companion.context
import java.io.File

/**
 * @author VIJAY PATIDAR
 */
object StorageManager {

    val userDir: File
        get() = context.getDir("userData", Context.MODE_PRIVATE)

    /**
     * @Return File  private directory of thumbnails
     */
    val thumbnails: File
        get() = context.getDir("thumbnails", Context.MODE_PRIVATE)

    val internal: File
        get() = File("/storage/emulated/0/")


    val external: File?
        get() {
            val externalStoragePath = KeyValue(context).externalStoragePath
            return if (externalStoragePath == null) {
                null
            } else {
                File(externalStoragePath)
            }
        }

    val downloadDir: File
        get() {
            val file = File(KeyValue(context).customStoragePath ?: "/storage/emulated/0/SendKr")
            if (!file.exists()) file.mkdirs()
            return file
        }


    // Checks if a volume containing external storage is available
    // for read and write.
    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

}