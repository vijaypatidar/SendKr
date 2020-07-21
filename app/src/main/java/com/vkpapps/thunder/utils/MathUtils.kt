package com.vkpapps.thunder.utils

import androidx.documentfile.provider.DocumentFile
import java.io.File

object MathUtils {
    fun roundTo(number: Double): Double {
        return (number * 100).toInt().toDouble() / 100
    }

    fun longToStringSize(length: Double): String {
        return when {
            length > 1073741824 -> {
                "${roundTo(length / (1073741824))} GB"
            }
            length > 1048576 -> {
                "${roundTo(length / (1048576))} MB"
            }
            length > 1024 -> {
                "${roundTo(length / (1024))} KB"
            }
            else -> {
                "${roundTo(length)} byte"
            }
        }
    }

    fun getFileSize(file: DocumentFile): String {
        var res = "0 byte"
        try {
            val length = File(file.uri.path!!).length().toDouble()
            res = MathUtils.longToStringSize(length)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return res
    }

    fun getFolderSize(file: DocumentFile): Long {
        var res: Long = 0
        try {
            if (file.isDirectory) {
                file.listFiles().forEach {
                    res += getFolderSize(it)
                }
            } else {
                return file.length()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return res
    }
}