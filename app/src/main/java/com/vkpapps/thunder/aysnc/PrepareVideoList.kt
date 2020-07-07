package com.vkpapps.thunder.aysnc

import android.os.AsyncTask
import android.provider.MediaStore
import com.vkpapps.thunder.App
import com.vkpapps.thunder.model.PhotoInfo
import com.vkpapps.thunder.model.VideoInfo

/***
 * @author VIJAY PATIDAR
 */
class PrepareVideoList(private val onVideoListPrepareListener: OnVideoListPrepareListener) : AsyncTask<Void?, Void?, List<VideoInfo>>() {

    override fun doInBackground(vararg params: Void?): List<VideoInfo> {
        val videoInfos = ArrayList<VideoInfo>()
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Video.VideoColumns.DATA, MediaStore.Video.VideoColumns.TITLE)
        val c = App.context.contentResolver.query(uri, projection, null, null, null)
        if (c != null) {
            while (c.moveToNext()) {
                val path = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                val name = c.getString(1).trim { it <= ' ' }
                if (path != null) {
                    val videoInfo = VideoInfo(name, path)
                    videoInfos.add(videoInfo)
                }
            }
            c.close()
        }

        return videoInfos
    }

    override fun onPostExecute(videoInfos: List<VideoInfo>) {
        super.onPostExecute(videoInfos)
        onVideoListPrepareListener.onVideoListPrepared(videoInfos)
    }

    interface OnVideoListPrepareListener {
        fun onVideoListPrepared(videoInfos: List<VideoInfo>)
    }

}