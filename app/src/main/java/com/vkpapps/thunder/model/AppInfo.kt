package com.vkpapps.thunder.model

import android.graphics.drawable.Drawable
import android.net.Uri

/***
 * @author VIJAY PATIDAR
 */
class AppInfo(var name: String, var uri: Uri, var icon: Drawable, var packageName: String) {
    var isSelected = false
    var isObbSelected = false
    var obbName: String? = null
    var obbUri: Uri? = null
    lateinit var size: String
    var obbSize: String = ""
}