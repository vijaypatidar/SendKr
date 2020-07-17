package com.vkpapps.thunder.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vkpapps.thunder.utils.MathUtils
import java.io.File

/***
 * @author VIJAY PATIDAR
 */
@Entity
class VideoInfo(var name: String, var path: String) {
    @PrimaryKey
    @NonNull
    lateinit var id: String
    var isSelected = false
    var size: String = MathUtils.longToStringSize(File(path).length().toDouble())
    var modified: Long = 0
}