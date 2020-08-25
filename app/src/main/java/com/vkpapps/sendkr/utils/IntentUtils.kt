package com.vkpapps.sendkr.utils

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toFile
import com.vkpapps.sendkr.analitics.Logger

object IntentUtils {
    fun startIntentToPlayVideo(context: Context, uri: Uri) {
        try {
            MediaScannerConnection.scanFile(context, arrayOf(uri.toFile().absolutePath), null) { _, resUri ->
                run {
                    val type = context.contentResolver.getType(uri)
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(resUri, type)
                    }
                    context.startActivity(intent)
                }
            }

        } catch (e: Exception) {
            Toast.makeText(context, "error occurred", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    fun startIntentToViewImage(context: Context, uri: Uri) {
        try {
            MediaScannerConnection.scanFile(context, arrayOf(uri.toFile().absolutePath), null) { _, resUri ->
                run {
                    val type = context.contentResolver.getType(uri)
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(resUri, type)
                    }
                    context.startActivity(intent)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "error occurred", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    fun startIntentToPlayAudio(context: Context, uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            MediaScannerConnection.scanFile(context, arrayOf(uri.path), null) { _, uri ->
                run {
                    val type = context.contentResolver.getType(uri)
                    Logger.d("file $uri type = $type")
                    intent.setDataAndType(uri, type)
                    context.startActivity(intent)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "error occurred", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}