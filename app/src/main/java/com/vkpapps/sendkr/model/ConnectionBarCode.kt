package com.vkpapps.sendkr.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ConnectionBarCode(
        @Expose
        @SerializedName(value = "ct") val connectionType: Int) {
    companion object {
        const val CONNECTION_EXTERNAL_AP = 1
        const val CONNECTION_INTERNAL_AP = 2
        const val CONNECTION_VIA_ROUTER = 3
    }

    @Expose
    @SerializedName(value = "ssid")
    var ssid: String? = null

    @Expose
    @SerializedName(value = "password")
    var password: String? = null

    @Expose
    @SerializedName(value = "ip")
    var ip: String? = null
}