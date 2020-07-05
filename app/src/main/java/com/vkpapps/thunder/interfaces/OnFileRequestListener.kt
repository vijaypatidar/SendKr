package com.vkpapps.thunder.interfaces

/**
 * @author VIJAY PATIDAR
 */
interface OnFileRequestListener {
    fun onDownloadRequest(name: String, id: String, type: Int)
    fun onDownloadRequestAccepted(name: String, id: String, type: Int)
    fun onUploadRequest(name: String, id: String, type: Int)
    fun onUploadRequestAccepted(name: String, id: String, type: Int)
}