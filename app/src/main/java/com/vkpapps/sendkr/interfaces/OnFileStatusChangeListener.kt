package com.vkpapps.sendkr.interfaces

import com.vkpapps.sendkr.model.RequestInfo

interface OnFileStatusChangeListener {
    fun onStatusChange(requestInfo: RequestInfo)
}