package com.vkpapps.thunder.interfaces

/***
 * @author VIJAY PATIDAR
 */
interface OnFileRequestReceiverListener {
    fun onRequestFailed(rid: String)
    fun onRequestAccepted(rid: String, cid: String, send: Boolean)
    fun onRequestSuccess(rid: String, timeTaken: Long, send: Boolean)
    fun onProgressChange(rid: String, transferred: Long)
}

