package com.vkpapps.sendkr.utils

import androidx.documentfile.provider.DocumentFile

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
                "${roundTo(length)} Bytes"
            }
        }
    }

    fun longToStringSizeGb(length: Double): String {
        return "${roundTo(length / (1073741824))}"
    }

    fun getFileSize(file: DocumentFile): Long {
        var res: Long = 0
        try {
            if (file.isDirectory) {
                file.listFiles().forEach {
                    res += getFileSize(it)
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