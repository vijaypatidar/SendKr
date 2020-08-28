package com.vkpapps.sendkr.utils

import android.content.Context
import java.io.File

/**
 * @author VIJAY PATIDAR
 */
class StorageManager(private val context: Context) {
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

    val profiles: File
        get() = context.getDir("profiles", Context.MODE_PRIVATE)

}