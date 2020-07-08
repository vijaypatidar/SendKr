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

    // Using LiveData and caching what getAlphabetizedWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
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
}
