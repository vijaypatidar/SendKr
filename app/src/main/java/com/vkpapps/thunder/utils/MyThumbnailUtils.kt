package com.vkpapps.thunder.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.os.Build
import android.os.CancellationSignal
import android.util.Size
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object MyThumbnailUtils {
    fun loadAudioThumbnail(file: File, source: String) {
        if (!file.exists()) {
            try {
                val mmr = MediaMetadataRetriever()
                mmr.setDataSource(source)
                val data = mmr.embeddedPicture
                if (data != null) {
                    val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, FileOutputStream(file))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadVideoThumbnail(file: File, path: String) {
        if (!file.exists()) {
            try {
                val mmr = MediaMetadataRetriever()
                mmr.setDataSource(path)
                mmr.frameAtTime?.compress(Bitmap.CompressFormat.JPEG, 90, FileOutputStream(file))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadPhotoThumbnail(file: File, path: String) {
        if (!file.exists()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    val thumbnail = ThumbnailUtils.createImageThumbnail(File(path), Size(250, 250), CancellationSignal())
                    thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, FileOutputStream(file))
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}