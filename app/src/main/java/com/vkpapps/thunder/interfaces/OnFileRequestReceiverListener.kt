package com.vkpapps.thunder.interfaces

import com.vkpapps.thunder.model.RequestInfo

/***
 * @author VIJAY PATIDAR
 */
interface OnFileRequestReceiverListener {
    fun onRequestFailed(requestInfo: RequestInfo)
    fun onRequestAccepted(requestInfo: RequestInfo)
    fun onRequestSuccess(requestInfo: RequestInfo, send: Boolean)
}

