package com.vkpapps.sendkr.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

@Entity
class HistoryInfo : Serializable {
    @PrimaryKey
    var id = ""
    lateinit var name: String
    lateinit var uri: Uri
    var date: Long = 0
    var type = 0

    @Ignore
    var isSelected: Boolean = false

    constructor()

    @Ignore
    constructor(id: String, name: String, uri: Uri, type: Int) {
        this.name = name
        this.id = id
        this.uri = uri
        this.type = type
        this.date = Date().time
    }
}