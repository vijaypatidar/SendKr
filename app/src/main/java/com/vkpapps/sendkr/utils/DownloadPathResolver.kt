package com.vkpapps.sendkr.utils

import com.vkpapps.sendkr.App
import com.vkpapps.sendkr.model.RequestInfo
import com.vkpapps.sendkr.model.constant.FileType
import java.io.File


/**
 * @author VIJAY PATIDAR
 *
 */
object DownloadPathResolver {
    private val root: File = StorageManager(App.context).downloadDir
    private fun getDirectory(type: Int): File {
        val file = when (type) {
            FileType.FILE_TYPE_MUSIC -> File(root, "musics")
            FileType.FILE_TYPE_APP -> File(root, "apps")
            FileType.FILE_TYPE_PHOTO -> File(root, "images")
            FileType.FILE_TYPE_VIDEO -> File(root, "videos")
            FileType.FILE_TYPE_FOLDER -> File(root, "folders")
            else -> File(root, "others")
        }
        file.exists().run {
            file.mkdirs()
        }
        return file
    }

    fun getSource(obj: RequestInfo): String {
        val directory = getDirectory(obj.fileType)
        var file = File(directory, obj.name)
        var i = 1
        while (file.exists()) {
            if (obj.fileType == FileType.FILE_TYPE_FOLDER) {
                file = File(directory, obj.name + "(${i++})")
            } else {
                val lastIndex: Int = obj.name.lastIndexOf(".")
                if (lastIndex < 0) {
                    obj.name += "(1)"
                } else {
                    val ext: String = obj.name.substring(lastIndex)
                    val nameWithoutExt: String = obj.name.substring(0, lastIndex)
                    file = File(directory, "$nameWithoutExt(${i++})$ext")
                }
            }
        }
        obj.name = file.name
        if (obj.fileType == FileType.FILE_TYPE_FOLDER) {
            file.mkdirs()
        }
        return file.absolutePath
    }
}