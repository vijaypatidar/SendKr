package com.vkpapps.sendkr.loader

import android.net.Uri
import android.provider.MediaStore
import com.vkpapps.sendkr.App
import com.vkpapps.sendkr.model.MediaInfo
import com.vkpapps.sendkr.utils.HashUtils
import com.vkpapps.sendkr.utils.MyThumbnailUtils
import java.io.File

/***
 * @author VIJAY PATIDAR
 */
class PrepareVideoList {

    fun getList(): List<MediaInfo> {
        val videoInfos = ArrayList<MediaInfo>()
        val projection = arrayOf(MediaStore.Video.VideoColumns.DATA, MediaStore.Video.VideoColumns.SIZE, MediaStore.Video.VideoColumns.DATE_MODIFIED)
        val c = App.context.contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, null)
        if (c != null) {
            while (c.moveToNext()) {
                val path = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                if (path != null) {
                    val file = File(path)
                    val uri = Uri.fromFile(file)
                    val lastModified = c.getLong(c.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED))
                    val size = c.getLong(c.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE))
                    val videoInfo = MediaInfo(uri, file.name, size, lastModified)
                    videoInfo.id = HashUtils.getHashValue(path.toByteArray())
                    videoInfos.add(videoInfo)
                }
            }
            c.close()
        }
        Thread {
            videoInfos.forEach {
                MyThumbnailUtils.loadVideoThumbnail(it.id, it.uri, null)
            }
        }.start()

        return videoInfos
    }
}