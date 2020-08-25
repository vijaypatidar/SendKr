package com.vkpapps.sendkr.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vkpapps.sendkr.model.VideoInfo

@Dao
interface VideoDao {

    @Query("SELECT * from videoinfo")
    fun getVideoInfos(): List<VideoInfo>

    @Query("SELECT * from videoinfo where id = :id")
    fun getVideoInfo(id: String): VideoInfo

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(requestInfo: VideoInfo)

    @Query("DELETE FROM videoinfo")
    suspend fun deleteAll()

    @Query("SELECT * from videoinfo order by lastModified DESC")
    fun getLiveVideoInfos(): LiveData<List<VideoInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(requestInfos: List<VideoInfo>)
}