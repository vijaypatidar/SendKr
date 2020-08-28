package com.vkpapps.sendkr.room.liveViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.vkpapps.sendkr.analitics.Logger
import com.vkpapps.sendkr.model.RequestInfo
import com.vkpapps.sendkr.model.constant.StatusType

class RequestViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        @JvmStatic
        private val requestInfos = ArrayList<RequestInfo>()

        @JvmStatic
        private val requestInfoMap = HashMap<String, RequestInfo>()

        @Volatile
        @JvmStatic
        private var pendingRequestCount = 0
    }

    val requestInfosLiveData = MutableLiveData(requestInfos)
    val pendingRequestCountLiveData = MutableLiveData(pendingRequestCount)

    fun insert(requestInfo: RequestInfo) {
        Logger.d("[RequestViewModel][insert] rid = ${requestInfo.rid}")
        synchronized(requestInfos) {
            requestInfos.add(requestInfo)
            requestInfoMap[requestInfo.rid] = requestInfo
            incrementPendingRequestCount()
        }
    }

    fun getRequestInfo(rid: String): RequestInfo? {
        Logger.d("[RequestViewModel][getRequestInfo] rid = $rid")
        synchronized(requestInfos) {
            return requestInfoMap[rid]
        }
    }

    fun requestCompleted(requestInfo: RequestInfo) {
        Logger.d("[RequestViewModel][requestCompleted] rid = ${requestInfo.rid}")
        if (requestInfo.status == StatusType.STATUS_COMPLETED) {
            synchronized(requestInfo) {
                requestInfoMap.remove(requestInfo.rid)
            }
        }
    }

    fun notifyDataSetChanged() {
        requestInfosLiveData.postValue(requestInfos)
    }

    fun notifyPendingCountChange() {
        pendingRequestCountLiveData.postValue(pendingRequestCount)
    }


    fun incrementPendingRequestCount() {
        pendingRequestCount++
        pendingRequestCountLiveData.postValue(pendingRequestCount)
    }

    fun decrementPendingRequestCount() {
        pendingRequestCount--
        pendingRequestCountLiveData.postValue(pendingRequestCount)
    }

    fun clearRequestList() {
        pendingRequestCount = 0
        requestInfos.clear()
        requestInfoMap.clear()
        notifyPendingCountChange()
        notifyDataSetChanged()
    }

}
