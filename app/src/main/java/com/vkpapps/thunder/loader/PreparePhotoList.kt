package com.vkpapps.thunder.loader

import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toFile
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
        val projection = arrayOf(MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.SIZE, MediaStore.Images.ImageColumns.DATE_MODIFIED)
        val c = App.context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null)
        if (c != null) {
            while (c.moveToNext()) {
                val path = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                if (path != null) {
                    val uri = Uri.fromFile(File(path))
                    val lastModified = c.getLong(c.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED))
                    val size = c.getLong(c.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE))
                    val file = uri.toFile()
                    val photoInfo = PhotoInfo(file.name, uri, size, lastModified)
                    photoInfo.id = HashUtils.getHashValue(path.toByteArray())
                    appInfos.add(photoInfo)
                }
            }
            c.close()
        }
        return appInfos
    }
}