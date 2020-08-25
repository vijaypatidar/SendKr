package com.vkpapps.sendkr.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vkpapps.sendkr.model.AudioInfo
import com.vkpapps.sendkr.model.HistoryInfo
import com.vkpapps.sendkr.model.PhotoInfo
import com.vkpapps.sendkr.model.VideoInfo
import com.vkpapps.sendkr.room.dao.AudioDao
import com.vkpapps.sendkr.room.dao.HistoryDao
import com.vkpapps.sendkr.room.dao.PhotoDao
import com.vkpapps.sendkr.room.dao.VideoDao
import com.vkpapps.sendkr.room.typeConverter.UriConverter

@Database(entities = [PhotoInfo::class, AudioInfo::class, VideoInfo::class, HistoryInfo::class], version = 1, exportSchema = false)
@TypeConverters(UriConverter::class)
abstract class MyRoomDatabase : RoomDatabase() {

    abstract fun photoDao(): PhotoDao
    abstract fun audioDao(): AudioDao
    abstract fun videoDao(): VideoDao
    abstract fun historyDao(): HistoryDao


    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: MyRoomDatabase? = null

        fun getDatabase(context: Context): MyRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        MyRoomDatabase::class.java,
                        "word_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}