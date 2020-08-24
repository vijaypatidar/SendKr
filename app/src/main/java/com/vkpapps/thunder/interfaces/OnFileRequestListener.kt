package com.vkpapps.thunder.interfaces

import com.vkpapps.thunder.connection.ClientHelper
import com.vkpapps.thunder.model.FileStatusRequest
import com.vkpapps.thunder.model.RequestInfo

/**
 * @author VIJAY PATIDAR
 */
interface OnFileRequestListener {
    fun onDownloadRequest(rid: String)
    fun onUploadRequest(rid: String)
    fun onNewRequestInfo(requestInfo: RequestInfo, clientHelper: ClientHelper)
    fun onFileStatusChange(fileStatusRequest: FileStatusRequest, clientHelper: ClientHelper)
}