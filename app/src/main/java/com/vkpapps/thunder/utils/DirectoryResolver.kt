package com.vkpapps.thunder.utils

import android.content.Context
import com.vkpapps.thunder.model.FileRequest
import java.io.File

/**
 * @author VIJAY PATIDAR
 */
class DirectoryResolver(private val context: Context){
    private val root : File = StorageManager(context).downloadDir
    fun  getDirectory(type:Int):File{
        val file = when(type){
            FileRequest.FILE_TYPE_MUSIC ->File(root,"Music")
            FileRequest.FILE_TYPE_PROFILE_PIC ->context.getDir("profiles",Context.MODE_PRIVATE)
            else->File(root,"others")
        }
        if (!file.exists())file.mkdirs()
        return file
    }
}