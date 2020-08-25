package com.vkpapps.sendkr.loader

import android.net.Uri
import android.provider.MediaStore
import com.vkpapps.sendkr.App
import com.vkpapps.sendkr.model.AudioInfo
import com.vkpapps.sendkr.utils.HashUtils
import com.vkpapps.sendkr.utils.MyThumbnailUtils
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
            }
            c.close()
            audioInfos.sortBy { it.name }
        }
        Thread {
            audioInfos.forEach {
                MyThumbnailUtils.loadAudioThumbnail(it.id, it.uri, null)
            }
        }.start()
        return audioInfos
    }
}