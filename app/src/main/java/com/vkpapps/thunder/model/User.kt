package com.vkpapps.thunder.model

import java.io.Serializable

/**
 * @author VIJAY PATIDAR
 */
class User : Serializable {
    var name: String = ""
    var userId: String = ""
    var appVersion: Int = 0
    var profileByteArray = ByteArray(0)
}