package com.vkpapps.thunder.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.vkpapps.thunder.utils.HashUtils
import java.io.Serializable
import java.util.*

@Entity
class HistoryInfo : Serializable {
    @PrimaryKey
    var id = ""
    lateinit var name: String
    lateinit var source: String
    var date: Long = 0
    var type = 0

    @Ignore
    var isSelected: Boolean = false

    constructor()

    @Ignore
    constructor(name: String, source: String, type: Int) {
        this.name = name
        this.id = HashUtils.getHashValue(source.toByteArray())
        this.source = source
        this.type = type
        this.date = Date().time
    }
}