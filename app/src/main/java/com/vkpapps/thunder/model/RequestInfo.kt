package com.vkpapps.thunder.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.vkpapps.thunder.utils.MathUtils
import java.io.File
import java.io.Serializable

@Entity
class RequestInfo : Serializable {
    @PrimaryKey
    var rid = ""
    lateinit var cid: String
    lateinit var name: String
    lateinit var source: String
    var fileType = 0
    var status = 0//pending
    var size: Long = 0
        set(value) {
            field = value
            displaySize = MathUtils.longToStringSize(size.toDouble())
        }
    var transferred: Long = 0
    var displaySize: String = ""



    constructor()

    @Ignore
    constructor(rid: String, cid: String, name: String, source: String, type: Int) {
        this.rid = rid
        this.cid = cid
        this.name = name
        this.source = source
        this.fileType = type
        this.size = File(this.source).length()
        this.displaySize = MathUtils.longToStringSize(size.toDouble())
        this.transferred = 0
    }

    fun clone(rid: String, cid: String): RequestInfo {
        return RequestInfo(rid, cid, name, source, fileType)
    }
}