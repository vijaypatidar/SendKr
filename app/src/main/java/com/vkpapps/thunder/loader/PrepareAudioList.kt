package com.vkpapps.thunder.loader

import android.net.Uri
import android.provider.MediaStore
import com.vkpapps.thunder.App
import com.vkpapps.thunder.model.AudioInfo
import com.vkpapps.thunder.utils.HashUtils
import java.io.File

/***
 * @author VIJAY PATIDAR
 */
class PrepareAudioList {
    fun getList(): List<AudioInfo> {
        val audioInfos = ArrayList<AudioInfo>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.AudioColumns.DATA, MediaStore.Audio.AudioColumns.TITLE)
        val c = App.context.contentResolver.query(uri, projection, null, null, null)
        if (c != null) {
            while (c.moveToNext()) {
                val path = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                val audioModel = AudioInfo(Uri.fromFile(File(path)), File(path).name)
                audioModel.id = HashUtils.getHashValue(path.toByteArray())
                audioInfos.add(audioModel)
                audioInfos.sortBy { it.name }
            }
            c.close()
        }
        return audioInfos
    }
}