package com.vkpapps.thunder.model

/***
 * @author VIJAY PATIDAR
 */
class FileRequest(var action: Int, var rid: String) {
    companion object {
        const val UPLOAD_REQUEST_CONFIRM = 14
        const val DOWNLOAD_REQUEST_CONFIRM = 15
    }
}