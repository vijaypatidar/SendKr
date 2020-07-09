package com.vkpapps.thunder.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vkpapps.thunder.service.FileService

/***
 * @author VIJAY PATIDAR
 */
class FileRequestReceiver(private val onFileRequestReceiverListener: OnFileRequestReceiverListener) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val rid = intent.getStringExtra(FileService.PARAM_RID)
        if (action == null || rid == null) return
        try {
            when (action) {
                FileService.STATUS_SUCCESS -> onFileRequestReceiverListener.onRequestSuccess(rid)
                FileService.STATUS_FAILED -> onFileRequestReceiverListener.onRequestFailed(rid)
                FileService.REQUEST_ACCEPTED -> {
                    val clientId = intent.getStringExtra(FileService.PARAM_CLIENT_ID)
                    onFileRequestReceiverListener.onRequestAccepted(rid, clientId!!)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    interface OnFileRequestReceiverListener {
        fun onRequestFailed(rid: String)
        fun onRequestAccepted(rid: String, cid: String)
        fun onRequestSuccess(rid: String)
    }

}