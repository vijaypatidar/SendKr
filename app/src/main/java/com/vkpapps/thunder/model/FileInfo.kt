package com.vkpapps.thunder.model

import androidx.documentfile.provider.DocumentFile
import com.vkpapps.thunder.analitics.Logger
import com.vkpapps.thunder.utils.HashUtils
import com.vkpapps.thunder.utils.MathUtils
import java.io.File

class FileInfo(var file: DocumentFile) {
    val id: String by lazy {
        HashUtils.getHashValue(file.uri.path!!.toByteArray())
    }
    val name: String? by lazy {
        file.name
    }
    var isSelected: Boolean = false
        set(value) {
            if (!isDirectory) {
                field = value
            }
        }
    val isDirectory: Boolean by lazy {
        file.isDirectory
    }
    val source: String? by lazy {
        file.uri.path
    }
    val size: String by lazy {
        var res = "0 byte"
        try {
            val length = File(file.uri.path).length().toDouble()
            res = MathUtils.longToStringSize(length)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        res
    }

    val fileCount: Int by lazy {
        file.listFiles().size
    }

    val type: Int by lazy {
        val type1 = file.type
        Logger.d("=========================================${type1}")
        when {
            type1 == null -> {
                FileType.FILE_TYPE_ANY
            }
            type1.contains("image") -> {
                FileType.FILE_TYPE_PHOTO
            }
            type1.contains("video") -> {
                FileType.FILE_TYPE_VIDEO
            }
            type1.contains("audio") -> {
                FileType.FILE_TYPE_MUSIC
            }
            else -> {
                FileType.FILE_TYPE_ANY
            }
        }
    }
}