package com.vkpapps.thunder.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

/***
 * @author VIJAY PATIDAR
 */
@Entity
class AudioInfo(var path: String, var name: String) {
    @PrimaryKey
    @NonNull
    lateinit var id: String
    var isSelected = false
}