package com.vkpapps.sendkr.model

import com.google.gson.annotations.Expose

/**
 * @author VIJAY PATIDAR
 */
class User {
    @Expose
    var name: String = ""

    @Expose
    var userId: String = ""

    @Expose
    var appVersion: Int = 0

    @Expose
    var profileByteArray = ByteArray(0)
}