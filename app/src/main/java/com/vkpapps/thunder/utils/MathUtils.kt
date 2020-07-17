package com.vkpapps.thunder.utils

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
}