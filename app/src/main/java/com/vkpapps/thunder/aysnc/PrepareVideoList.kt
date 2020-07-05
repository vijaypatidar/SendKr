package com.vkpapps.thunder.aysnc

import android.os.AsyncTask
import android.provider.MediaStore
import com.vkpapps.thunder.App
import com.vkpapps.thunder.model.PhotoInfo
/***
 * @author VIJAY PATIDAR
 */
class PrepareVideoList(private val onVideoListPrepareListener: OnVideoListPrepareListener) : AsyncTask<Void?, Void?, List<PhotoInfo>>() {

    override fun doInBackground(vararg params: Void?): List<PhotoInfo> {
        val appInfos = ArrayList<PhotoInfo>()
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Video.VideoColumns.DATA, MediaStore.Video.VideoColumns.TITLE)
        val c = App.context.contentResolver.query(uri, projection, null, null, null)
        if (c != null) {
            while (c.moveToNext()) {
                val path = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                val name = c.getString(1).trim { it <= ' ' }
                if (path != null){
                    val photoInfo = PhotoInfo(name, path)
                    appInfos.add(photoInfo)
                }
            }
            c.close()
        }

        return appInfos
    }

    override fun onPostExecute(photoInfos:List<PhotoInfo>) {
        super.onPostExecute(photoInfos)
        onVideoListPrepareListener.onVideoListPrepared(photoInfos)
    }

    interface OnVideoListPrepareListener {
        fun onVideoListPrepared(photoInfos:List<PhotoInfo>)
    }

}