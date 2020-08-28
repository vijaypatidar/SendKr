package com.vkpapps.sendkr.interfaces

import com.vkpapps.sendkr.model.RequestInfo

/***
 * @author VIJAY PATIDAR
 */
interface OnFileRequestReceiverListener {
    fun onRequestFailed(requestInfo: RequestInfo)
    fun onRequestAccepted(requestInfo: RequestInfo)
    fun onRequestSuccess(requestInfo: RequestInfo, send: Boolean)
}

