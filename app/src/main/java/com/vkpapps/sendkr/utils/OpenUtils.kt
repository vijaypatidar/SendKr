package com.vkpapps.sendkr.utils

import android.content.Context
import android.net.Uri
import com.vkpapps.sendkr.model.constant.FileType

object OpenUtils {
    fun open(fileType: Int, context: Context, uri: Uri) {
        when (fileType) {
            FileType.FILE_TYPE_APP -> {
                IntentUtils.startAppInstallIntent(context, uri)
            }
            else -> {
                IntentUtils.startActionViewIntent(context, uri)
            }
        }
    }

}