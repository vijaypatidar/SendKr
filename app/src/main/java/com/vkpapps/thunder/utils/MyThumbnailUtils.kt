package com.vkpapps.thunder.utils

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toFile
import com.squareup.picasso.Picasso
import com.vkpapps.thunder.App
import com.vkpapps.thunder.R
import com.vkpapps.thunder.model.constant.FileType
import java.io.File

object MyThumbnailUtils {
    private val thumbnails = StorageManager(App.context).thumbnails

    fun loadAudioThumbnail(id: String, uri: Uri, imageView: AppCompatImageView?) {
        val file = File(thumbnails, id)
        try {
            if (!file.exists()) {
                val mmr = MediaMetadataRetriever().apply { setDataSource(App.context, uri) }
                val data = mmr.embeddedPicture
                val bitmap = BitmapFactory.decodeByteArray(data, 0, data!!.size)
                BitmapUtils.bitmapToFile(bitmap, file)
                imageView?.run {
                    Picasso.get().load(file).centerCrop().into(imageView)
                }
                mmr.close()
            } else {
                imageView?.run {
                    Picasso.get().load(file).into(imageView)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            imageView?.setImageResource(R.drawable.ic_default_audio_icon)
        }

    }

    fun loadVideoThumbnail(id: String, uri: Uri, imageView: ImageView?) {
        val file = File(thumbnails, id)
        try {
            if (!file.exists()) {
                val mmr = MediaMetadataRetriever()
                mmr.setDataSource(App.context, uri)
                mmr.frameAtTime?.run {
                    BitmapUtils.bitmapToFile(this, file)
                }
                imageView?.run {
                    Picasso.get().load(file).into(imageView)
                }
            } else {
                imageView?.run {
                    Picasso.get().load(file).into(imageView)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            imageView?.setImageResource(R.drawable.ic_movie)

        }
    }

    fun loadApkThumbnail(id: String, uri: Uri, imageView: ImageView?) {
        val file = File(thumbnails, id)
        if (!file.exists()) {
            try {
                val absolutePath = uri.toFile().absolutePath
                val packageManager = App.context.packageManager
                val packageArchiveInfo = packageManager.getPackageArchiveInfo(absolutePath, 0)!!.apply {
                    applicationInfo.publicSourceDir = absolutePath
                    applicationInfo.sourceDir = absolutePath
                }
                val loadIcon = packageArchiveInfo.applicationInfo.loadIcon(App.context.packageManager)
                BitmapUtils.bitmapToFile(loadIcon.toBitmap(), file)
                imageView?.run {
                    Picasso.get().load(file).into(imageView)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                imageView?.setImageResource(R.drawable.ic_android)
            }

        } else {
            imageView?.run {
                Picasso.get().load(file).into(imageView)
            }
        }

    }


    fun loadPhotoThumbnail(uri: Uri, imageView: AppCompatImageView?) {
        Picasso.get().load(uri).centerCrop().resize(256, 256).into(imageView)
    }


    fun loadThumbnail(id: String, uri: Uri, type: Int, logo: AppCompatImageView?) {
        when (type) {
            FileType.FILE_TYPE_PHOTO -> {
                loadPhotoThumbnail(uri, logo)
            }
            FileType.FILE_TYPE_VIDEO -> {
                loadVideoThumbnail(id, uri, logo)
            }
            FileType.FILE_TYPE_MUSIC -> {
                loadAudioThumbnail(id, uri, logo)
            }
            FileType.FILE_TYPE_APP -> {
                loadApkThumbnail(id, uri, logo)
            }
            FileType.FILE_TYPE_FOLDER -> {
                logo?.setImageResource(R.drawable.ic_folder)
            }
            else -> logo?.setImageResource(R.drawable.ic_file)
        }
    }


}