package com.vkpapps.thunder.room.liveViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.vkpapps.thunder.model.VideoInfo
import com.vkpapps.thunder.room.database.MyRoomDatabase
import com.vkpapps.thunder.room.repository.VideoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VideoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VideoRepository = VideoRepository(MyRoomDatabase.getDatabase(application).videoDao())

    public val videoInfos: LiveData<List<VideoInfo>>

    init {
        videoInfos = repository.allVideoInfo
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insert(videoInfo: VideoInfo) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(videoInfo)
    }

    fun insertAll(videoInfo: List<VideoInfo>) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertAll(videoInfo)
    }
}
