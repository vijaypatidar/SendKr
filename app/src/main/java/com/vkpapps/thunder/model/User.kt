package com.vkpapps.thunder.model

/**
 * @author VIJAY PATIDAR
 */
class User {
    var name: String = ""
    var userId: String = ""
    var appVersion: Int = 0
    var profileByteArray = ByteArray(0)

    fun copyFrom(user: User) {
        name = user.name
        appVersion = user.appVersion
        profileByteArray = user.profileByteArray
    }
}