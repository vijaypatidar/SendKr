package com.vkpapps.sendkr.room.repository

import androidx.lifecycle.LiveData
import com.vkpapps.sendkr.model.PhotoInfo
import com.vkpapps.sendkr.room.dao.PhotoDao

class PhotoRepository(private val photoDao: PhotoDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val allPhotoInfo: LiveData<List<PhotoInfo>> = photoDao.getLivePhotoInfos()

    suspend fun insert(photoInfo: PhotoInfo) {
        photoDao.insert(photoInfo)
    }

    fun insertAll(requestInfos: List<PhotoInfo>) {
        photoDao.insertAll(requestInfos)
    }
}