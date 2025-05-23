package com.vkpapps.sendkr.utils

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toFile
import com.squareup.picasso.Picasso
import com.vkpapps.sendkr.App
import com.vkpapps.sendkr.R
import com.vkpapps.sendkr.model.constant.FileType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executors

object MyThumbnailUtils {
    private val thumbnails = StorageManager.thumbnails
    private val loader = Executors.newFixedThreadPool(2).asCoroutineDispatcher()

    fun loadAudioThumbnail(id: String, uri: Uri, imageView: AppCompatImageView?) {
        val file = File(thumbnails, id)
        if (!file.exists()) {
            CoroutineScope(loader).launch {
                try {
                    val mmr = MediaMetadataRetriever().apply { setDataSource(App.context, uri) }
                    val data = mmr.embeddedPicture!!
                    val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                    BitmapUtils.bitmapToFile(bitmap, file)
                    mmr.release()
                    Picasso.get().invalidate(file)
                    imageView?.run {
                        withContext(Main) {
                            Picasso.get().load(file).into(imageView)
                        }
                    }
                } catch (e: Exception) {
                    withContext(Main) {
                        imageView?.setImageResource(R.drawable.ic_default_audio_icon)
                    }
                }
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
            CoroutineScope(loader).launch {
                try {
                    val mmr = MediaMetadataRetriever()
                    mmr.setDataSource(App.context, uri)
                    mmr.frameAtTime?.run {
                        withContext(Main) {
                            imageView?.setImageBitmap(this@run)
                        }
                        BitmapUtils.bitmapToFile(this, file)
                        Picasso.get().invalidate(file)
                    }
                    mmr.release()
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Main) {
                        imageView?.setImageResource(R.drawable.ic_movie)
                    }
                }
            }
        } else {
            imageView?.run {
                Picasso.get().load(file).into(imageView)
            }
        }
    }

    fun loadApkThumbnail(id: String, uri: Uri, imageView: ImageView?) {
        val file = File(thumbnails, id)
        if (!file.exists()) {
            CoroutineScope(loader).launch {
                try {
                    val absolutePath = uri.toFile().absolutePath
                    val packageManager = App.context.packageManager
                    val packageArchiveInfo = packageManager.getPackageArchiveInfo(absolutePath, 0)!!.apply {
                        applicationInfo.publicSourceDir = absolutePath
                        applicationInfo.sourceDir = absolutePath
                    }
                    val loadIcon = packageArchiveInfo.applicationInfo.loadIcon(App.context.packageManager)
                    withContext(Main) {
                        imageView?.setImageDrawable(loadIcon)
                    }
                    BitmapUtils.bitmapToFile(loadIcon.toBitmap(), file)
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Main) {
                        imageView?.setImageResource(R.drawable.ic_android)
                    }
                }

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