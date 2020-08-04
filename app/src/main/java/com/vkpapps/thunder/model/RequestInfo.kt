package com.vkpapps.thunder.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.vkpapps.thunder.utils.MathUtils
import java.io.Serializable

@Entity
class RequestInfo : Serializable {

    @PrimaryKey
    var rid: String = ""
    var cid: String = ""
    var name: String = ""
    lateinit var uri: String
    var fileType = 0
    var status = 0//pending
    var size: Long = 0
        set(value) {
            displaySize = MathUtils.longToStringSize(value.toDouble())
            field = value
        }
    var transferred: Long = 0
    var displaySize: String = ""


    constructor()

    @Ignore
    constructor(rid: String, cid: String, name: String, uri: String, type: Int, size: Long) {
        this.rid = rid
        this.cid = cid
        this.name = name
        this.uri = uri
        this.fileType = type
        this.size = size
    }

    fun clone(rid: String, cid: String): RequestInfo {
        return RequestInfo(rid, cid, name, uri, fileType, size)
    }
}