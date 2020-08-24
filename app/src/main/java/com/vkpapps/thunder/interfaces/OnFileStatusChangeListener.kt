package com.vkpapps.thunder.interfaces

import com.vkpapps.thunder.model.RequestInfo

interface OnFileStatusChangeListener {
    fun onStatusChange(requestInfo: RequestInfo)
}