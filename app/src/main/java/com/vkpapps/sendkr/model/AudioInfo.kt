package com.vkpapps.sendkr.model

import android.net.Uri
import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

/***
 * @author VIJAY PATIDAR
 */
@Entity
class AudioInfo(val uri: Uri, val name: String, val size: Long, val lastModified: Long) {
    @PrimaryKey
    @NonNull
    lateinit var id: String
    var isSelected = false
}