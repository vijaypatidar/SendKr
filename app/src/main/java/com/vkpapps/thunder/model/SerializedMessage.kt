package com.vkpapps.thunder.model

import com.google.gson.annotations.SerializedName

/**
 * @author VIJAY-PATIDAR
 */
class SerializedMessage {
    @SerializedName("user")
    var user: User? = null

    @SerializedName("requestInfo")
    var requestInfo: RequestInfo? = null

    @SerializedName("fileRequest")
    var fileRequest: FileRequest? = null

    constructor(fileRequest: FileRequest?) {
        this.fileRequest = fileRequest
    }

    constructor(user: User?) {
        this.user = user
    }

    constructor(requestInfo: RequestInfo?) {
        this.requestInfo = requestInfo
    }

}