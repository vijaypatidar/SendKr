package com.vkpapps.sendkr.loader

import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toFile
import com.vkpapps.sendkr.App
import com.vkpapps.sendkr.model.MediaInfo
import com.vkpapps.sendkr.utils.HashUtils
import java.io.File

/***
 * @author VIJAY PATIDAR
 */
class PreparePhotoList {
    fun getList(): List<MediaInfo> {
        val appInfos = ArrayList<MediaInfo>()
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
                    val photoInfo = MediaInfo(uri, file.name, size, lastModified)
                    photoInfo.id = HashUtils.getHashValue(path.toByteArray())
                    appInfos.add(photoInfo)
                }
            }
            c.close()
        }
        return appInfos
    }
}