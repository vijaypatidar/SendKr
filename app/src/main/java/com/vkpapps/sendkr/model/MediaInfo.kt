package com.vkpapps.sendkr.model

import android.net.Uri

/***
 * @author VIJAY PATIDAR
 */
class MediaInfo(val uri: Uri, val name: String, val size: Long, val lastModified: Long) {
    lateinit var id: String
    var isSelected = false
}