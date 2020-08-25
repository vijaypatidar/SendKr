package com.vkpapps.sendkr.interfaces

import com.vkpapps.sendkr.model.RawRequestInfo

/***
 * @author VIJAY PATIDAR
 */
interface OnFileRequestPrepareListener {
    fun sendFiles(requests: List<RawRequestInfo>)
}