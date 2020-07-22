package com.vkpapps.thunder.model

import androidx.documentfile.provider.DocumentFile
import com.vkpapps.thunder.model.constaints.FileType
import com.vkpapps.thunder.utils.HashUtils
import com.vkpapps.thunder.utils.MathUtils
import com.vkpapps.thunder.utils.MimeTypeResolver

class FileInfo(var file: DocumentFile) {

    val id: String by lazy {
        HashUtils.getHashValue(file.uri.path!!.toByteArray())
    }
    val name: String? by lazy {
        file.name
    }
    var isSelected: Boolean = false
    val isDirectory: Boolean = file.isDirectory

    val source: String? = file.uri.path

    val displaySize: String by lazy {
        MathUtils.getFileDisplaySize(file)
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

    val type: Int by lazy { if (file.isDirectory) FileType.FILE_TYPE_FOLDER else MimeTypeResolver.getFileType(file.type) }
}