package com.vkpapps.thunder.loader

import android.provider.MediaStore
import com.vkpapps.thunder.App
import com.vkpapps.thunder.model.PhotoInfo
import com.vkpapps.thunder.utils.HashUtils
import java.io.File

/***
 * @author VIJAY PATIDAR
 */
class PreparePhotoList {
    fun getList(): List<PhotoInfo> {
        val appInfos = ArrayList<PhotoInfo>()
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.ImageColumns.DATA)
        val c = App.context.contentResolver.query(uri, projection, null, null, null)
        if (c != null) {
            while (c.moveToNext()) {
                val path = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                if (path != null) {
                    val file = File(path)
                    val photoInfo = PhotoInfo(file.name, path)
                    photoInfo.modified = file.lastModified()
                    photoInfo.id = HashUtils.getHashValue(path.toByteArray())
                    appInfos.add(photoInfo)
                }
            }
            c.close()
        }
        return appInfos
    }
}