package com.vkpapps.sendkr.utils

import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.core.os.EnvironmentCompat
import com.vkpapps.sendkr.App.Companion.context
import java.io.File

/**
 * @author VIJAY PATIDAR
 */
object StorageManager {

    val userDir: File
        get() = File(context.noBackupFilesDir, "userData").apply {
            mkdirs()
        }

    /**
     * @Return File  private directory of thumbnails
     */
    val thumbnails: File
        get() = File(context.cacheDir, "thumbnails").apply { mkdirs() }

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
    fun isRemovableSdCardMounted(): Boolean {
        val externalFilesDirs = ContextCompat.getExternalFilesDirs(context, null)
        return (externalFilesDirs.size >= 2 && EnvironmentCompat.getStorageState(File(removableSdCardRootPath())) == Environment.MEDIA_MOUNTED)
    }

    fun removableSdCardRootPath(): String {
        val sdCard = ContextCompat.getExternalFilesDirs(context, null)[1].absolutePath
        val indexOf = sdCard.indexOf("/Android")
        return if (indexOf != -1)
            sdCard.subSequence(0, indexOf).toString()
        else sdCard
    }

}