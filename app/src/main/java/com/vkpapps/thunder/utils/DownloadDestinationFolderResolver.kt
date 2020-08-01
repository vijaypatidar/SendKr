package com.vkpapps.thunder.utils

import com.vkpapps.thunder.model.constaints.FileType

object DownloadDestinationFolderResolver {
    fun getFileType(mimeType: String?): Int {
        return when {
            mimeType == null -> {
                FileType.FILE_TYPE_ANY
            }
            mimeType.startsWith("image") -> {
                FileType.FILE_TYPE_PHOTO
            }
            mimeType.startsWith("video") -> {
                FileType.FILE_TYPE_VIDEO
            }
            mimeType.startsWith("audio") -> {
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