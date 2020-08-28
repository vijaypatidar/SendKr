package com.vkpapps.sendkr.utils

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toFile

object IntentUtils {
    fun startAppInstallIntent(context: Context, uri: Uri) {
        try {
            MediaScannerConnection.scanFile(context, arrayOf(uri.toFile().absolutePath), null) { _, uriRes ->
                run {
                    val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                        setDataAndType(uriRes, "application/vnd.android.package-archive")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "error occurred", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    fun startActionViewIntent(context: Context, uri: Uri) {
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
}