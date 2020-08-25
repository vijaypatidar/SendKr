package com.vkpapps.sendkr.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vkpapps.sendkr.model.AudioInfo

@Dao
interface AudioDao {

    @Query("SELECT * from audioinfo")
    fun getAudioInfos(): List<AudioInfo>

    @Query("SELECT * from audioinfo where id = :id")
    fun getAudioInfo(id: String): AudioInfo

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(audioInfo: AudioInfo)

    @Query("DELETE FROM audioinfo")
    suspend fun deleteAll()

    @Query("SELECT * from audioinfo")
    fun getLiveAudioInfos(): LiveData<List<AudioInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(requestInfos: List<AudioInfo>)
}