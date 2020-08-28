package com.vkpapps.sendkr.interfaces

import com.vkpapps.sendkr.model.MediaInfo

/**
 * @author VIJAY PATIDAR
 */
interface OnMediaSelectListener {
    fun onMediaLongClickListener(mediaInfo: MediaInfo)
    fun onMediaSelected(mediaInfo: MediaInfo)
    fun onMediaDeselected(mediaInfo: MediaInfo)
}