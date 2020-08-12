package com.vkpapps.thunder.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.vkpapps.thunder.connection.ClientHelper

class AppLiveData(application: Application) : AndroidViewModel(application) {
    val pendingRequestLiveData = MutableLiveData<ArrayList<RawRequestInfo>>(ArrayList())
    val clientHelpersLiveData = MutableLiveData<ArrayList<ClientHelper>>(ArrayList())

    fun addClient(clientHelper: ClientHelper) {
        this.clientHelpersLiveData.value?.add(clientHelper)
        this.clientHelpersLiveData.postValue(clientHelpersLiveData.value)
    }

    fun removeClient(clientHelper: ClientHelper) {
        this.clientHelpersLiveData.value?.remove(clientHelper)
        this.clientHelpersLiveData.postValue(clientHelpersLiveData.value)
    }

    fun notifyUserInformationChanged() {
        this.clientHelpersLiveData.postValue(clientHelpersLiveData.value)
    }

    fun addRequests(rawRequestInfos: List<RawRequestInfo>) {
        pendingRequestLiveData.value?.addAll(rawRequestInfos)
        pendingRequestLiveData.postValue(pendingRequestLiveData.value)
    }

    fun clearPendingQueue() {
        pendingRequestLiveData.postValue(ArrayList())
    }

}