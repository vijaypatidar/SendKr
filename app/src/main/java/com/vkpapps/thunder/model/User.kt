package com.vkpapps.thunder.model

import java.io.Serializable

/**
 * @author VIJAY PATIDAR
 */
class User : Serializable {
    lateinit var name: String
    lateinit var userId: String
    var appVersion: Int = 0
    var profileByteArray = ByteArray(0)
}