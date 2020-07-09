package com.vkpapps.thunder.room.repository

import androidx.lifecycle.LiveData
import com.vkpapps.thunder.model.VideoInfo
import com.vkpapps.thunder.room.dao.VideoDao

class VideoRepository(private val videoDao: VideoDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val allVideoInfo: LiveData<List<VideoInfo>> = videoDao.getLiveVideoInfos()

    suspend fun insert(videoInfo: VideoInfo) {
        videoDao.insert(videoInfo)
    }

    fun insertAll(videoInfos: List<VideoInfo>) {
        videoDao.insertAll(videoInfos)
    }
}