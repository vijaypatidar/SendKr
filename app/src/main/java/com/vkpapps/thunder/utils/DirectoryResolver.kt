package com.vkpapps.thunder.utils

import android.content.Context
import com.vkpapps.thunder.model.FileType
import com.vkpapps.thunder.model.RequestInfo
import java.io.File

/**
 * @author VIJAY PATIDAR
 */
class DirectoryResolver(private val context: Context){
    private val root : File = StorageManager(context).downloadDir
    private fun getDirectory(type: Int): File {
        val file = when (type) {
            FileType.FILE_TYPE_MUSIC -> File(root, "musics")
            FileType.FILE_TYPE_APP -> File(root, "apps")
            FileType.FILE_TYPE_PHOTO -> File(root, "images")
            FileType.FILE_TYPE_VIDEO -> File(root, "videos")
            FileType.FILE_TYPE_PROFILE_PIC -> context.getDir("profiles", Context.MODE_PRIVATE)
            else -> File(root, "others")
        }
        file.exists().run {
            file.mkdirs()
        }
        return file
    }

    fun getSource(obj: RequestInfo): String {
        val file = File(getDirectory(obj.type), obj.name)
//        if (file.exists()){
//        //todo check file if exist
//        }
        return file.absolutePath
    }
}