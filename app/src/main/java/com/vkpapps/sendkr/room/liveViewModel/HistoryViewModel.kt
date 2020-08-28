package com.vkpapps.sendkr.room.liveViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.vkpapps.sendkr.model.HistoryInfo
import com.vkpapps.sendkr.room.database.MyRoomDatabase
import com.vkpapps.sendkr.room.repository.HistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HistoryRepository = HistoryRepository(MyRoomDatabase.getDatabase(application).historyDao())

    val historyInfos: LiveData<List<HistoryInfo>>

    init {
        historyInfos = repository.liveHistoryInfos
    }

    fun insert(historyInfo: HistoryInfo) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(historyInfo)
    }

    fun deleteAll() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAll()
    }

    fun delete(id: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(id)
    }

}
