package com.vkpapps.thunder.model

class ConnectionBarCode(val connectionType: Int) {
    companion object {
        const val CONNECTION_EXTERNAL_AP = 1
        const val CONNECTION_INTERNAL_AP = 2
        const val CONNECTION_VIA_ROUTER = 3
    }

    var ssid: String? = null
    var password: String? = null
    var ip: Int? = null
}