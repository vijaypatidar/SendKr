package com.vkpapps.sendkr.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/***
 * @author VIJAY PATIDAR
 */
class FileRequest(@Expose @SerializedName(value = "send") var send: Boolean, @Expose @SerializedName(value = "rid") var rid: String)