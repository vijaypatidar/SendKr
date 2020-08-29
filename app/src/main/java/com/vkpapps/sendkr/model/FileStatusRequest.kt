package com.vkpapps.sendkr.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/***
 * @author VIJAY PATIDAR
 */
class FileStatusRequest(@Expose @SerializedName(value = "status") var status: Int, @Expose @SerializedName(value = "rid") var rid: String)