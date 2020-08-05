package com.vkpapps.thunder.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.vkpapps.thunder.utils.MathUtils

class RequestInfo {

    @SerializedName("rid")
    var rid: String = ""

    @SerializedName("cid")
    var cid: String = ""

    @SerializedName("fileName")
    var name: String = ""

    @SerializedName("fileType")
    var fileType = 0

    @SerializedName("size")
    var size: Long = 0
        set(value) {
            displaySize = MathUtils.longToStringSize(value.toDouble())
            field = value
        }

    @SerializedName("displaySize")
    var displaySize: String = ""

    @Expose(serialize = false, deserialize = false)
    lateinit var uri: String

    @Expose(serialize = false, deserialize = false)
    var status = 0//pending
    var transferred: Long = 0

    constructor()

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