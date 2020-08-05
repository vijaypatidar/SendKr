package com.vkpapps.thunder.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import com.squareup.picasso.Picasso
import com.vkpapps.thunder.App
import com.vkpapps.thunder.R
import com.vkpapps.thunder.model.constaints.FileType
import java.io.File
import java.io.FileOutputStream

object MyThumbnailUtils {
    private val thumbnails = StorageManager(App.context).thumbnails

    fun loadAudioThumbnail(id: String, uri: Uri, imageView: AppCompatImageView?) {
        val file = File(thumbnails, id)
        if (!file.exists()) {
            try {
                val mmr = MediaMetadataRetriever()
                mmr.setDataSource(App.context, uri)
                val data = mmr.embeddedPicture
                val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, FileOutputStream(file))
                imageView?.run {
                    Picasso.get().load(file).centerCrop().into(imageView)
                }
                mmr.close()
            } catch (e: Exception) {
                e.printStackTrace()
                imageView?.setImageResource(R.drawable.ic_default_audio_icon)
            }
        } else {
            imageView?.run {
                Picasso.get().load(file).into(imageView)
            }
        }
    }

    fun loadVideoThumbnail(id: String, uri: Uri, imageView: ImageView?) {
        val file = File(thumbnails, id)
        if (!file.exists()) {
            try {
                val mmr = MediaMetadataRetriever()
                mmr.setDataSource(App.context, uri)
                mmr.frameAtTime?.compress(Bitmap.CompressFormat.JPEG, 90, FileOutputStream(file))
                imageView?.run {
                    Picasso.get().load(file).centerCrop().into(imageView)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                imageView?.setImageResource(R.drawable.ic_movie)
            }
        } else {
            imageView?.run {
                Picasso.get().load(file).into(imageView)
            }
        }
    }

    fun loadPhotoThumbnail(id: String, uri: Uri, imageView: AppCompatImageView?) {
        Picasso.get().load(uri).centerCrop().resize(256, 256).into(imageView)
    }


    fun loadThumbnail(id: String, uri: Uri, type: Int, logo: AppCompatImageView?) {
        when (type) {
            FileType.FILE_TYPE_PHOTO -> {
                loadPhotoThumbnail(id, uri, logo)
            }
            FileType.FILE_TYPE_VIDEO -> {
                loadVideoThumbnail(id, uri, logo)
            }
            FileType.FILE_TYPE_MUSIC -> {
                loadAudioThumbnail(id, uri, logo)
            }
            FileType.FILE_TYPE_APP -> {
                logo?.setImageResource(R.drawable.ic_apps)
            }
            FileType.FILE_TYPE_FOLDER -> {
                logo?.setImageResource(R.drawable.ic_folder)
            }
            else -> logo?.setImageResource(R.drawable.ic_file)
        }
    }


}