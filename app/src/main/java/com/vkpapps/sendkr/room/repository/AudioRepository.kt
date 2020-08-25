package com.vkpapps.sendkr.room.repository

import androidx.lifecycle.LiveData
import com.vkpapps.sendkr.model.AudioInfo
import com.vkpapps.sendkr.room.dao.AudioDao

class AudioRepository(private val audioDao: AudioDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val allAudioInfo: LiveData<List<AudioInfo>> = audioDao.getLiveAudioInfos()

    suspend fun insert(audioInfo: AudioInfo) {
        audioDao.insert(audioInfo)
    }

    fun insertAll(audioInfos: List<AudioInfo>) {
        audioDao.insertAll(audioInfos)
    }
}