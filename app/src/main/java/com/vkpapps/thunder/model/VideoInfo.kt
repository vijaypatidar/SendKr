package com.vkpapps.thunder.model

import android.net.Uri
import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

/***
 * @author VIJAY PATIDAR
 */
@Entity
class VideoInfo(var name: String, var uri: Uri, val size: Long, val lastModified: Long) {
    @PrimaryKey
    @NonNull
    lateinit var id: String
    var isSelected = false
}