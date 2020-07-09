package com.vkpapps.thunder.loader

import com.vkpapps.thunder.App
import com.vkpapps.thunder.room.database.MyRoomDatabase
import com.vkpapps.thunder.room.repository.AudioRepository
import com.vkpapps.thunder.room.repository.PhotoRepository
import com.vkpapps.thunder.room.repository.VideoRepository

class PrepareDb() {
    suspend fun prepareAll() {
        val database = MyRoomDatabase.getDatabase(App.context)
        val audioDao = database.audioDao()
        audioDao.deleteAll()
        AudioRepository(audioDao).insertAll(PrepareAudioList().getList())

        val videoDao = database.videoDao()
        videoDao.deleteAll()
        VideoRepository(videoDao).insertAll(PrepareVideoList().getList())

        val photoDao = database.photoDao()
        photoDao.deleteAll()
        PhotoRepository(photoDao).insertAll(PreparePhotoList().getList())
    }
}