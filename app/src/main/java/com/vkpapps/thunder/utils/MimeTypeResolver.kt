package com.vkpapps.thunder.utils

import com.vkpapps.thunder.model.FileType

object MimeTypeResolver {
    fun getFileType(mimeType: String?): Int {
        return when {
            mimeType == null -> {
                FileType.FILE_TYPE_ANY
            }
            mimeType.contains("image") -> {
                FileType.FILE_TYPE_PHOTO
            }
            mimeType.contains("video") -> {
                FileType.FILE_TYPE_VIDEO
            }
            mimeType.contains("audio") -> {
                FileType.FILE_TYPE_MUSIC
            }
            mimeType.contains("application/vnd.android.package-archive") -> {
                FileType.FILE_TYPE_APP
            }
            else -> {
                FileType.FILE_TYPE_ANY
            }
        }
    }
}