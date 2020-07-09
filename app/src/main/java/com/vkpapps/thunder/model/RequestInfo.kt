package com.vkpapps.thunder.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
class RequestInfo : Serializable {
    @PrimaryKey
    var rid = ""
    lateinit var cid: String
    lateinit var name: String
    lateinit var source: String
    var type = 0
    var status = 0
    var requestType = 0

    constructor()

    @Ignore
    constructor(rid: String, cid: String, name: String, source: String, type: Int, requestType: Int) {
        this.rid = rid
        this.cid = cid
        this.name = name
        this.source = source
        this.type = type
        status = 0 //pending
        this.requestType = requestType
    }

    fun clone(rid: String, cid: String): RequestInfo {
        return RequestInfo(rid, cid, name, source, type, requestType)
    }
}