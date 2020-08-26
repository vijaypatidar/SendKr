package com.vkpapps.sendkr.model

import com.google.gson.annotations.Expose

class RequestInfo {
    @Expose
    var rid: String = ""

    var cid: String = ""

    @Expose
    var name: String = ""

    //used to decide destination folder
    @Expose
    var fileType = 0

    @Expose
    var size: Long = 0

    var send: Boolean = false

    // generated at receiver side after checking file exists status is false
    var uri: String? = null
    var status = 0//pending

    //
    var transferred: Long = 0


    fun clone(): RequestInfo {
        val clone = RequestInfo()
        clone.rid = rid
        clone.send = send
        clone.cid = cid
        clone.name = name
        clone.fileType = fileType
        clone.size = size
        clone.uri = uri
        clone.status = status
        clone.transferred = transferred
        return clone
    }

    override fun equals(other: Any?): Boolean {
        return when {
            other == null -> {
                false
            }
            (other as RequestInfo).rid == rid -> {
                true
            }
            else -> false
        }
    }

    override fun hashCode(): Int {
        return rid.hashCode()
    }
}