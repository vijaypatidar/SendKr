package com.vkpapps.thunder.room.repository

import androidx.lifecycle.LiveData
import com.vkpapps.thunder.model.RequestInfo
import com.vkpapps.thunder.room.dao.RequestDao

class RequestRepository(private val requestDao: RequestDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val allRequestInfo: LiveData<List<RequestInfo>> = requestDao.getLiveRequestInfos()

    suspend fun insert(requestInfo: RequestInfo) {
        requestDao.insert(requestInfo)
    }

    fun insertAll(requestInfos: List<RequestInfo>) {
        requestDao.insertAll(requestInfos)
    }
}