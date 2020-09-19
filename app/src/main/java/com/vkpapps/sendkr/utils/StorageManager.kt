package com.vkpapps.sendkr.utils

import android.os.Environment
import androidx.core.net.toFile
import androidx.core.os.EnvironmentCompat
import androidx.documentfile.provider.DocumentFile
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
            DocumentFile.fromFile(File("/storage/")).listFiles().forEach {
                if (Environment.isExternalStorageRemovable(it.uri.toFile().absoluteFile)) {
                    return it.uri.toFile()
                }
            }
            return null
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
        return try {
            EnvironmentCompat.getStorageState(File(removableSdCardRootPath())) == Environment.MEDIA_MOUNTED
        } catch (e: Exception) {
            false
        }
    }

    fun removableSdCardRootPath(): String {
        return external?.absolutePath ?: ""
    }

}