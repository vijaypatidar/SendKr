package com.vkpapps.thunder.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vkpapps.thunder.model.*
import com.vkpapps.thunder.room.dao.*

@Database(entities = [RequestInfo::class, PhotoInfo::class, AudioInfo::class, VideoInfo::class, HistoryInfo::class], version = 1, exportSchema = false)
public abstract class MyRoomDatabase : RoomDatabase() {

    abstract fun requestDao(): RequestDao
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