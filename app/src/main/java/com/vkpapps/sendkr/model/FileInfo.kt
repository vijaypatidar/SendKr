package com.vkpapps.sendkr.model

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.vkpapps.sendkr.model.constant.FileType
import com.vkpapps.sendkr.utils.DownloadDestinationFolderResolver
import com.vkpapps.sendkr.utils.HashUtils

class FileInfo(var file: DocumentFile, val size: Long) {

    val id: String by lazy {
        HashUtils.getHashValue(file.uri.path!!.toByteArray())
    }

    var isSelected: Boolean = false
    val uri: Uri = file.uri

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

    val type: Int by lazy { if (file.isDirectory) FileType.FILE_TYPE_FOLDER else DownloadDestinationFolderResolver.getFileType(file.type) }
}