package com.vkpapps.thunder.aysnc

import android.os.AsyncTask
import android.provider.MediaStore
import com.vkpapps.thunder.App
import com.vkpapps.thunder.model.PhotoInfo

/***
 * @author VIJAY PATIDAR
 */
class PreparePhotoList(private val onPhotoListPrepareListener: OnPhotoListPrepareListener) : AsyncTask<Void?, Void?, List<PhotoInfo>>() {

    override fun doInBackground(vararg params: Void?): List<PhotoInfo> {
        val appInfos = ArrayList<PhotoInfo>()

        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.TITLE)
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

    override fun onPostExecute(photoInfo: List<PhotoInfo>) {
        super.onPostExecute(photoInfo)
        onPhotoListPrepareListener.onPhotoListPrepared(photoInfo)
    }

    interface OnPhotoListPrepareListener {
        fun onPhotoListPrepared(photoInfo: List<PhotoInfo>)
    }

}