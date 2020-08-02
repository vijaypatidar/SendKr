package com.vkpapps.thunder.loader

import android.net.Uri
import android.provider.MediaStore
import com.vkpapps.thunder.App
import com.vkpapps.thunder.analitics.Logger
import com.vkpapps.thunder.model.AudioInfo
import com.vkpapps.thunder.utils.HashUtils
import com.vkpapps.thunder.utils.MyThumbnailUtils
import java.io.File

/***
 * @author VIJAY PATIDAR
 */
class PrepareAudioList {
    fun getList(): List<AudioInfo> {
        val audioInfos = ArrayList<AudioInfo>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.AudioColumns.DATA, MediaStore.Audio.AudioColumns.SIZE, MediaStore.Audio.AudioColumns.DATE_ADDED)
        val c = App.context.contentResolver.query(uri, projection, null, null, null)
        if (c != null) {
            while (c.moveToNext()) {
                val path = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                val lastModified = c.getLong(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED))
                val size = c.getLong(c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))
                val audioModel = AudioInfo(Uri.fromFile(File(path)), File(path).name, size, lastModified)
                audioModel.id = HashUtils.getHashValue(path.toByteArray())
                audioInfos.add(audioModel)
                Logger.d("size audio $size")
            }
            c.close()
            audioInfos.sortBy { it.name }
        }
        Thread(Runnable {
            audioInfos.forEach {
                MyThumbnailUtils.loadAudioThumbnail(it.id, it.uri, null)
            }
        }).start()
        return audioInfos
    }
}