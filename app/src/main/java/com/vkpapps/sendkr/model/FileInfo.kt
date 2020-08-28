package com.vkpapps.sendkr.model

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.vkpapps.sendkr.model.constant.FileType
import com.vkpapps.sendkr.utils.FileTypeResolver
import com.vkpapps.sendkr.utils.HashUtils
import com.vkpapps.sendkr.utils.MathUtils

class FileInfo(var file: DocumentFile) {

    val id: String by lazy {
        HashUtils.getHashValue(file.uri.path!!.toByteArray())
    }

    var isSelected: Boolean = false
    val uri: Uri = file.uri

    val size by lazy { MathUtils.getFileSize(file) }
    val name: String by lazy {
        file.name ?: ""
    }

    val lastModified: Long by lazy {
        file.lastModified()
    }

    val fileCount: Int by lazy {
        var res = 0
        try {
            res = file.listFiles().size
        } catch (e: Exception) {
            e.printStackTrace()
        }
        res
    }

    val type: Int by lazy { if (file.isDirectory) FileType.FILE_TYPE_FOLDER else FileTypeResolver.getFileType(file.type) }
}