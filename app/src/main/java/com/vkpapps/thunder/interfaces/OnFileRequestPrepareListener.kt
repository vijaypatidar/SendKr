package com.vkpapps.thunder.interfaces

import com.vkpapps.thunder.model.RawRequestInfo

/***
 * @author VIJAY PATIDAR
 */
interface OnFileRequestPrepareListener {
    fun sendFiles(requests: List<RawRequestInfo>)
}