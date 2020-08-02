package com.vkpapps.thunder.model

import android.net.Uri
import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.vkpapps.thunder.utils.MathUtils

/***
 * @author VIJAY PATIDAR
 */
@Entity
class VideoInfo(var name: String, var uri: Uri, val size: Long, val lastModified: Long) {
    @PrimaryKey
    @NonNull
    lateinit var id: String
    var isSelected = false
    var modified: Long = 0

    @delegate:Ignore
    val displaySize: String by lazy {
        MathUtils.longToStringSize(size.toDouble())
    }
}