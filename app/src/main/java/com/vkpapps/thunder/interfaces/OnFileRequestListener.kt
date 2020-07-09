package com.vkpapps.thunder.interfaces

import com.vkpapps.thunder.model.RequestInfo

/**
 * @author VIJAY PATIDAR
 */
interface OnFileRequestListener {
    fun onDownloadRequest(rid: String)
    fun onUploadRequest(rid: String)
    fun onNewRequestInfo(obj: RequestInfo)
}