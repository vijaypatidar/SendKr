package com.vkpapps.thunder.room.repository

import com.vkpapps.thunder.model.HistoryInfo
import com.vkpapps.thunder.room.dao.HistoryDao

class HistoryRepository(private val historyDao: HistoryDao) {

    val liveHistoryInfos = historyDao.getLiveHistoryInfos()

    suspend fun insert(historyInfo: HistoryInfo) {
        historyDao.insert(historyInfo)
    }

    suspend fun deleteAll() {
        historyDao.deleteAll()
    }
}