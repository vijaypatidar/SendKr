package com.vkpapps.thunder.utils

import android.content.Context
import com.vkpapps.thunder.analitics.Logger
import com.vkpapps.thunder.model.RequestInfo
import com.vkpapps.thunder.model.constaints.FileType
import java.io.File

/**
 * @author VIJAY PATIDAR
 */
class DirectoryResolver(private val context: Context) {
    private val root: File = StorageManager(context).downloadDir
    private fun getDirectory(type: Int): File {
        val file = when (type) {
            FileType.FILE_TYPE_MUSIC -> File(root, "musics")
            FileType.FILE_TYPE_APP -> File(root, "apps")
            FileType.FILE_TYPE_PHOTO -> File(root, "images")
            FileType.FILE_TYPE_VIDEO -> File(root, "videos")
            FileType.FILE_TYPE_FOLDER -> File(root, "folders")
            FileType.FILE_TYPE_PROFILE_PIC -> context.getDir("profiles", Context.MODE_PRIVATE)
            else -> File(root, "others")
        }
        file.exists().run {
            file.mkdirs()
        }
        return file
    }

    fun getSource(obj: RequestInfo): String {
        var file = File(getDirectory(obj.type), obj.name)
        // in case of folder request make dir for it
        if (file.exists()) {
            if (obj.type == FileType.FILE_TYPE_FOLDER) {
                var i = 0
                while (!file.exists()) {
                    obj.source = obj.source + "(${i++})"
                    file = File(obj.source)
                }
            } else {
                //todo check file if exist
            }
        }
        if (obj.type == FileType.FILE_TYPE_FOLDER) {
            file.mkdirs()
            Logger.d("New dir created ${obj.source}")
        }
        return file.absolutePath
    }
}