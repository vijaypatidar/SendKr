package com.vkpapps.thunder.room.liveViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.vkpapps.thunder.model.RequestInfo
import com.vkpapps.thunder.room.database.MyRoomDatabase
import com.vkpapps.thunder.room.repository.RequestRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RequestViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RequestRepository

    val allRequestInfo: LiveData<List<RequestInfo>>

    init {
        val requestDao = MyRoomDatabase.getDatabase(application).requestDao()
        repository = RequestRepository(requestDao)
        allRequestInfo = repository.allRequestInfo
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insert(requestInfo: RequestInfo) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(requestInfo)
    }

    fun insertAll(requestInfos: List<RequestInfo>) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertAll(requestInfos)
    }

    fun updateStatus(rid: String, status: Int) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateStatus(rid, status)
    }

    fun updateProgress(rid: String, transferred: Long) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateProgress(rid, transferred)
    }
}
