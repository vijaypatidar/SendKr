package com.vkpapps.thunder.model

import android.net.Uri
import androidx.annotation.NonNull
import androidx.core.net.toFile
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vkpapps.thunder.utils.MathUtils

/***
 * @author VIJAY PATIDAR
 */
@Entity
class VideoInfo(var name: String, var uri: Uri) {
    @PrimaryKey
    @NonNull
    lateinit var id: String
    var isSelected = false
    var size: String = MathUtils.longToStringSize(uri.toFile().length().toDouble())
    var modified: Long = 0
}