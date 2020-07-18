package com.vkpapps.thunder.model

import androidx.documentfile.provider.DocumentFile
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
        set(value) {
            if (!isDirectory) {
                field = value
            }
        }
    val isDirectory: Boolean = file.isDirectory

    val source: String? = file.uri.path

    val size: String = MathUtils.getFileSize(file)

    val fileCount: Int by lazy {
        var res = 0
        try {
            res = file.listFiles().size
        } catch (e: Exception) {
            e.printStackTrace()
        }
        res
    }


    val type: Int = MimeTypeResolver.getFileType(file.type)
}