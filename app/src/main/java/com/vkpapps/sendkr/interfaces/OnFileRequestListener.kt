package com.vkpapps.sendkr.interfaces

import com.vkpapps.sendkr.connection.ClientHelper
import com.vkpapps.sendkr.model.FileStatusRequest
import com.vkpapps.sendkr.model.RequestInfo

/**
 * @author VIJAY PATIDAR
 */
interface OnFileRequestListener {
    fun onDownloadRequest(rid: String)
    fun onUploadRequest(rid: String)
    fun onNewRequestInfo(requestInfo: RequestInfo, clientHelper: ClientHelper)
    fun onFileStatusChange(fileStatusRequest: FileStatusRequest, clientHelper: ClientHelper)
}