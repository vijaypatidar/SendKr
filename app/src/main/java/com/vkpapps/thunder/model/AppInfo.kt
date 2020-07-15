package com.vkpapps.thunder.model

import android.graphics.drawable.Drawable

/***
 * @author VIJAY PATIDAR
 */
class AppInfo(var name: String, var source: String, var icon: Drawable, var packageName: String) {
    var isSelected = false
    var isObbSelected = false
    var obbName: String? = null
    var obbSource: String? = null
}