package com.vkpapps.thunder.model

import androidx.documentfile.provider.DocumentFile

class FileInfo(var file: DocumentFile) {
    val name: String? by lazy {
        file.name
    }
    var isSelected: Boolean = false
        set(value) {
            if (!isDirectory) {
                field = value
            }
        }
    val isDirectory: Boolean by lazy {
        file.isDirectory
    }
    val source: String? by lazy {
        file.uri.path
    }
    val type: Int = FileType.FILE_TYPE_ANY
}