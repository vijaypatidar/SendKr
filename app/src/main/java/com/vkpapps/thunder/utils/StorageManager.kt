package com.vkpapps.thunder.utils

import android.content.Context
import android.os.Environment
import com.vkpapps.thunder.analitics.Logger
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
        get() = context.filesDir.absoluteFile

    val external: File?
        get() = context.getExternalFilesDir(null)?.absoluteFile

    val downloadDir: File
        get() {
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Thunder")
            Logger.d("==========================>${file.absolutePath}")
            if (!file.exists()) file.mkdirs()
            return file
        }

    val profiles: File
        get() = context.getDir("profiles", Context.MODE_PRIVATE)

}