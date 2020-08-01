package com.vkpapps.thunder.loader

import android.net.Uri
import android.provider.MediaStore
import com.vkpapps.thunder.App
import com.vkpapps.thunder.model.VideoInfo
import com.vkpapps.thunder.utils.HashUtils
import java.io.File

/***
 * @author VIJAY PATIDAR
 */
class PrepareVideoList {

    fun getList(): List<VideoInfo> {
        val videoInfos = ArrayList<VideoInfo>()
        val projection = arrayOf(MediaStore.Video.VideoColumns.DATA)
        val c = App.context.contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, null)
        if (c != null) {
            while (c.moveToNext()) {
                val path = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                if (path != null) {
                    val file = File(path)
                    val uri = Uri.fromFile(file)
                    val videoInfo = VideoInfo(file.name, uri)
                    videoInfo.id = HashUtils.getHashValue(path.toByteArray())
                    videoInfo.modified = file.lastModified()
                    videoInfos.add(videoInfo)
                }
            }
            c.close()
        }

        return videoInfos
    }
}