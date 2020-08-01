package com.vkpapps.thunder.room.liveViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.vkpapps.thunder.model.PhotoInfo
import com.vkpapps.thunder.room.database.MyRoomDatabase
import com.vkpapps.thunder.room.repository.PhotoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PhotoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PhotoRepository = PhotoRepository(MyRoomDatabase.getDatabase(application).photoDao())

    public val photoInfos: LiveData<List<PhotoInfo>>

    init {
        photoInfos = repository.allPhotoInfo
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insert(photoInfo: PhotoInfo) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(photoInfo)
    }

    fun insertAll(photoInfo: List<PhotoInfo>) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertAll(photoInfo)
    }
}
