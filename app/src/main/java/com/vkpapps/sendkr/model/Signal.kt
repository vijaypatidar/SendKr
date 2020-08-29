package com.vkpapps.sendkr.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Signal {
    @Expose
    @SerializedName(value = "type")
    var type = -1

    @Expose
    @SerializedName(value = "requestInfo")
    var requestInfo: RequestInfo? = null

    @Expose
    @SerializedName(value = "fileRequest")
    var fileRequest: FileRequest? = null

    @Expose
    @SerializedName(value = "user")
    var user: User? = null

    @Expose
    @SerializedName(value = "fileStatusRequest")
    var fileStatusRequest: FileStatusRequest? = null

    constructor(requestInfo: RequestInfo) {
        this.requestInfo = requestInfo
        this.type = INPUT_TYPE_REQUEST_INFO
    }

    constructor(user: User?) {
        this.user = user
        this.type = INPUT_TYPE_USER
    }

    constructor(fileStatusRequest: FileStatusRequest?) {
        this.fileStatusRequest = fileStatusRequest
        this.type = INPUT_TYPE_FILE_STATUS_REQUEST
    }

    constructor(fileRequest: FileRequest?) {
        this.fileRequest = fileRequest
        this.type = INPUT_TYPE_FILE_REQUEST
    }

    companion object {
        const val INPUT_TYPE_FILE_REQUEST = 0
        const val INPUT_TYPE_REQUEST_INFO = 1
        const val INPUT_TYPE_FILE_STATUS_REQUEST = 2
        const val INPUT_TYPE_USER = 3
    }
}