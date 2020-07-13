package com.vkpapps.thunder.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import com.squareup.picasso.Picasso
import com.vkpapps.thunder.R
import com.vkpapps.thunder.model.FileType
import java.io.File
import java.io.FileOutputStream

object MyThumbnailUtils {
    fun loadAudioThumbnail(file: File, source: String, imageView: AppCompatImageView) {
        if (!file.exists()) {
            try {
                val mmr = MediaMetadataRetriever()
                mmr.setDataSource(source)
                val data = mmr.embeddedPicture
                val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, FileOutputStream(file))
                Picasso.get().load(file).into(imageView)
            } catch (e: Exception) {
                e.printStackTrace()
                imageView.setImageResource(R.drawable.ic_default_audio_icon)
            }
        } else {
            Picasso.get().load(file).into(imageView)
        }
    }

    fun loadVideoThumbnail(file: File, path: String, imageView: ImageView) {
        if (!file.exists()) {
            try {
                val mmr = MediaMetadataRetriever()
                mmr.setDataSource(path)
                mmr.frameAtTime?.compress(Bitmap.CompressFormat.JPEG, 90, FileOutputStream(file))
                Picasso.get().load(file).into(imageView)
            } catch (e: Exception) {
                e.printStackTrace()
                imageView.setImageResource(R.drawable.ic_movie)
            }
        } else {
            Picasso.get().load(file).into(imageView)
        }
    }

    fun loadPhotoThumbnail(path: String, imageView: AppCompatImageView) {
        Picasso.get().load(File(path)).centerCrop().resize(256, 256).into(imageView)
    }

    fun loadThumbnail(icon: File, source: String, type: Int, logo: AppCompatImageView) {
        when (type) {
            FileType.FILE_TYPE_PHOTO -> {
                loadPhotoThumbnail(source, logo)
            }
            FileType.FILE_TYPE_VIDEO -> {
                loadVideoThumbnail(icon, source, logo)
            }
            FileType.FILE_TYPE_MUSIC -> {
                loadAudioThumbnail(icon, source, logo)
            }
            FileType.FILE_TYPE_APP -> {
                logo.setImageResource(R.drawable.ic_apps)
            }
            else -> logo.setImageResource(R.drawable.ic_file)
        }
    }


}