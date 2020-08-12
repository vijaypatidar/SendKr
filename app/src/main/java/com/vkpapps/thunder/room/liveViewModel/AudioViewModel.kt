package com.vkpapps.thunder.room.liveViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.vkpapps.thunder.model.AudioInfo
import com.vkpapps.thunder.room.database.MyRoomDatabase
import com.vkpapps.thunder.room.repository.AudioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AudioRepository = AudioRepository(MyRoomDatabase.getDatabase(application).audioDao())

    val audioInfos: LiveData<List<AudioInfo>>

    init {
        audioInfos = repository.allAudioInfo
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insert(audioInfo: AudioInfo) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(audioInfo)
    }

    fun insertAll(audioInfo: List<AudioInfo>) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertAll(audioInfo)
    }
}
