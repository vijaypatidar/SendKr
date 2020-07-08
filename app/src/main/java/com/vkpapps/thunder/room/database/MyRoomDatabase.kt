package com.vkpapps.thunder.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vkpapps.thunder.model.AudioInfo
import com.vkpapps.thunder.model.PhotoInfo
import com.vkpapps.thunder.model.RequestInfo
import com.vkpapps.thunder.model.VideoInfo
import com.vkpapps.thunder.room.dao.AudioDao
import com.vkpapps.thunder.room.dao.PhotoDao
import com.vkpapps.thunder.room.dao.RequestDao
import com.vkpapps.thunder.room.dao.VideoDao

@Database(entities = [RequestInfo::class, PhotoInfo::class, AudioInfo::class, VideoInfo::class], version = 1, exportSchema = false)
public abstract class MyRoomDatabase : RoomDatabase() {

    abstract fun requestDao(): RequestDao
    abstract fun photoDao(): PhotoDao
    abstract fun audioDao(): AudioDao
    abstract fun videoDao(): VideoDao

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