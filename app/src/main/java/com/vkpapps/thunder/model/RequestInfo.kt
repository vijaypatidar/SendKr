package com.vkpapps.thunder.model

import java.io.Serializable

class RequestInfo : Serializable {
    var rid: String = ""
    var sid: String = ""
    var cid: String = ""
    var name: String = ""

    //used to decide destination folder
    var fileType = 0
    var size: Long = 0
    var displaySize: String = ""

    // generated at receiver side after checking file exists status is false
    var uri: String? = null
    var status = 0//pending

    //
    var transferred: Long = 0


    fun clone(): RequestInfo {
        val clone = RequestInfo()
        clone.rid = rid
        clone.sid = sid
        clone.cid = cid
        clone.name = name
        clone.fileType = fileType
        clone.size = size
        clone.displaySize = displaySize
        clone.uri = uri
        clone.status = status
        clone.transferred = transferred
        return clone
    }
}