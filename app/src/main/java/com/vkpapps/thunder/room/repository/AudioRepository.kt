package com.vkpapps.thunder.room.repository

import androidx.lifecycle.LiveData
import com.vkpapps.thunder.model.AudioInfo
import com.vkpapps.thunder.room.dao.AudioDao

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