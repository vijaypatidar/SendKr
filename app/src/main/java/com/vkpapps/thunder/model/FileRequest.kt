package com.vkpapps.thunder.model

import java.io.Serializable

/***
 * @author VIJAY PATIDAR
 */
class FileRequest(var action: Int, var rid: String) : Serializable {
    companion object {
        const val UPLOAD_REQUEST_CONFIRM = 14
        const val DOWNLOAD_REQUEST_CONFIRM = 15
    }
}