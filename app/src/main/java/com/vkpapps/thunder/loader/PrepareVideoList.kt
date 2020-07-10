package com.vkpapps.thunder.loader

import android.provider.MediaStore
import com.vkpapps.thunder.App
import com.vkpapps.thunder.model.VideoInfo
import com.vkpapps.thunder.utils.HashUtils
import java.io.File

/***
 * @author VIJAY PATIDAR
 */
class PrepareVideoList() {

    fun getList(): List<VideoInfo> {
        val videoInfos = ArrayList<VideoInfo>()
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Video.VideoColumns.DATA, MediaStore.Video.VideoColumns.TITLE)
        val c = App.context.contentResolver.query(uri, projection, null, null, null)
        if (c != null) {
            while (c.moveToNext()) {
                val path = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                if (path != null) {
                    val videoInfo = VideoInfo(File(path).name, path)
                    videoInfo.id = HashUtils.getHashValue(path.toByteArray())
                    videoInfos.add(videoInfo)
                }
            }
            c.close()
        }

        return videoInfos
    }
}