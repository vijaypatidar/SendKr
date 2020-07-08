package com.vkpapps.thunder.loader

import com.vkpapps.thunder.App
import com.vkpapps.thunder.room.database.MyRoomDatabase

class PrepareDb() {
    suspend fun prepareAll() {
        val database = MyRoomDatabase.getDatabase(App.context)
        val audioDao = database.audioDao()
        audioDao.deleteAll()
        audioDao.insertAll(PrepareAudioList().getList())

        val videoDao = database.videoDao()
        videoDao.deleteAll()
        videoDao.insertAll(PrepareVideoList().getList())

        val photoDao = database.photoDao()
        photoDao.deleteAll()
        photoDao.insertAll(PreparePhotoList().getList())
    }
}