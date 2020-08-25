package com.vkpapps.sendkr.loader

import com.vkpapps.sendkr.App
import com.vkpapps.sendkr.room.database.MyRoomDatabase
import com.vkpapps.sendkr.room.repository.AudioRepository
import com.vkpapps.sendkr.room.repository.PhotoRepository
import com.vkpapps.sendkr.room.repository.VideoRepository

class PrepareDb {
    suspend fun prepareAll() {
        prepareAudio()
        prepareVideo()
        preparePhoto()
        PrepareAppList.appList
    }

    suspend fun prepareAudio() {
        val database = MyRoomDatabase.getDatabase(App.context)
        val audioDao = database.audioDao()
        audioDao.deleteAll()
        AudioRepository(audioDao).insertAll(PrepareAudioList().getList())

    }

    suspend fun preparePhoto() {
        val database = MyRoomDatabase.getDatabase(App.context)
        val photoDao = database.photoDao()
        photoDao.deleteAll()
        PhotoRepository(photoDao).insertAll(PreparePhotoList().getList())
    }

    suspend fun prepareVideo() {
        val database = MyRoomDatabase.getDatabase(App.context)
        val videoDao = database.videoDao()
        videoDao.deleteAll()
        VideoRepository(videoDao).insertAll(PrepareVideoList().getList())
    }
}