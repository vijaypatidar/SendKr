package com.vkpapps.thunder.room.liveViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.vkpapps.thunder.model.RequestInfo

class RequestViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        @JvmStatic
        val requestInfos = ArrayList<RequestInfo>()

        @Volatile
        @JvmStatic
        private var pendingRequestCount = 0
    }

    val requestInfosLiveData = MutableLiveData(requestInfos)
    val pendingRequestCountLiveData = MutableLiveData(pendingRequestCount)

    fun insert(obj: RequestInfo) {
        requestInfos.add(obj)
        incrementPendingRequestCount()
    }

    fun getRequestInfo(rid: String): RequestInfo? {
        try {
            for (i in requestInfos.indices) {
                if (requestInfos[i].rid == rid) return requestInfos[i]
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun notifyDataSetChanged() {
        requestInfosLiveData.postValue(requestInfos)
    }

    fun notifyPendingCountChange() {
        pendingRequestCountLiveData.postValue(pendingRequestCount)
    }


    private fun incrementPendingRequestCount() {
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
        notifyPendingCountChange()
        notifyDataSetChanged()
    }

}
