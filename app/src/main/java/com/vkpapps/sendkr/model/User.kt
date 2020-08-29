package com.vkpapps.sendkr.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * @author VIJAY PATIDAR
 */
class User {
    @Expose
    @SerializedName(value = "user")
    var name: String = ""

    @Expose
    @SerializedName(value = "userId")
    var userId: String = ""

    @Expose
    @SerializedName(value = "appVersion")
    var appVersion: Int = 0

    @Expose
    @SerializedName(value = "profileByteArray")
    var profileByteArray = ByteArray(0)
}