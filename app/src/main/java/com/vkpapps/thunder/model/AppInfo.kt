package com.vkpapps.thunder.model

import android.net.Uri
import com.vkpapps.thunder.utils.HashUtils

/***
 * @author VIJAY PATIDAR
 */
class AppInfo(var name: String, var uri: Uri, var packageName: String) {
    var id = HashUtils.getHashValue(uri.toString().toByteArray())
    var isSelected = false
    var isObbSelected = false
    var obbName: String? = null
    var obbUri: Uri? = null
    lateinit var size: String
    var obbSize: String = ""
}