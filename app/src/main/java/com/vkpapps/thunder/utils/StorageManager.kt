package com.vkpapps.thunder.utils

import android.app.Activity
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import com.vkpapps.thunder.model.AudioInfo
import com.vkpapps.thunder.utils.PermissionUtils.checkStoragePermission
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

/**
 * @author VIJAY PATIDAR
 */
class StorageManager(private val context: Context) {
    val userDir: File
        get() = context.getDir("userData", Context.MODE_PRIVATE)

    /**
     * @Return File  private directory of thumbnails
     */
    val imageDir: File
        get() = context.getDir("images", Context.MODE_PRIVATE)

    val songDir: File
        get() = context.getDir("songs", Context.MODE_PRIVATE)

     val downloadDir: File
        get(){
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Thunder")
            if (!file.exists())file.mkdirs()
            return file
        }

    fun deleteDir(dir: File) {
        try {
            if (dir.isDirectory) {
                val songs = dir.listFiles()
                if (songs != null) {
                    for (s in songs) {
                        s.delete()
                    }
                }
            }
        } catch (ignored: Exception) {
        }
    }


     fun copyFile(source: File, destination: File, onStorageManagerListener: OnStorageManagerListener?) {
        Thread(Runnable {
            try {
                val fileInputStream = FileInputStream(source)
                val fileOutputStream = FileOutputStream(destination)
                val bytes = ByteArray(2048)
                var read: Int
                while (fileInputStream.read(bytes).also { read = it } > 0) {
                    fileOutputStream.write(bytes, 0, read)
                }
                fileOutputStream.flush()
                fileOutputStream.close()
                fileInputStream.close()

                // run on ui if context is activity
                if (context is Activity) {
                    context.runOnUiThread { onStorageManagerListener?.onCopyComplete(source) }
                } else {
                    // else on this thread
                    onStorageManagerListener?.onCopyComplete(source)
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }).start()
    }

    val profiles: File
        get() = context.getDir("profiles", Context.MODE_PRIVATE)

    interface OnStorageManagerListener {
        fun onCopyComplete(source: File?)
    }

    // inserting null value , this null values will be replaced by adView when displayed on RecyclerView
    val allAudioFromDevice: List<AudioInfo?>
        get() {
            if (audioInfos.size == 0) {
                if (checkStoragePermission(context)) {
                    audioInfos = ArrayList()
                    val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    val projection = arrayOf(MediaStore.Audio.AudioColumns.DATA, MediaStore.Audio.AudioColumns.TITLE)
                    val c = context.contentResolver.query(uri, projection, null, null, null)
                    if (c != null) {
                        while (c.moveToNext()) {
                            val path = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                            val name = c.getString(1).trim { it <= ' ' }
                            val audioModel = AudioInfo()
                            audioModel.name = name
                            audioModel.path = path
                            audioInfos.add(audioModel)
                            Collections.sort(audioInfos, Comparator { o1: AudioInfo?, o2: AudioInfo? -> o1!!.name.compareTo(o2!!.name) })
                        }
                        c.close()
                    }
                }
                // inserting null value , this null values will be replaced by adView when displayed on RecyclerView
                var i = 5
                while (i < audioInfos.size) {
                    audioInfos.add(i, null)
                    i += 45
                }
            }
            return audioInfos
        }

    companion object {
        private var audioInfos: MutableList<AudioInfo?> = ArrayList()
    }

}