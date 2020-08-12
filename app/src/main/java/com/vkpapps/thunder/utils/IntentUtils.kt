package com.vkpapps.thunder.utils

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import androidx.core.net.toFile

object IntentUtils {
    fun startIntentToPlayVideo(context: Context, uri: Uri) {
        MediaScannerConnection.scanFile(context, arrayOf(uri.toFile().absolutePath), null) { _, resUri ->
            run {
                val type = context.contentResolver.getType(uri)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(resUri, type)
                }
                context.startActivity(Intent.createChooser(intent, "Play with"))
            }
        }
    }
}