package com.vkpapps.thunder.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

/***
 * @author VIJAY PATIDAR
 */
@Entity
class PhotoInfo(var name: String, var path: String) {
    @PrimaryKey
    @NonNull
    lateinit var id: String
    var isSelected = false
    var modified: Long = 0
}