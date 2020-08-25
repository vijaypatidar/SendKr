package com.vkpapps.sendkr.room.repository

import com.vkpapps.sendkr.model.HistoryInfo
import com.vkpapps.sendkr.room.dao.HistoryDao

class HistoryRepository(private val historyDao: HistoryDao) {

    val liveHistoryInfos = historyDao.getLiveHistoryInfos()

    suspend fun insert(historyInfo: HistoryInfo) {
        historyDao.insert(historyInfo)
    }

    suspend fun deleteAll() {
        historyDao.deleteAll()
    }

    suspend fun delete(id: String) {
        historyDao.delete(id)
    }
}