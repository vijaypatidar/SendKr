package com.vkpapps.thunder.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vkpapps.thunder.model.PhotoInfo
import com.vkpapps.thunder.model.VideoInfo

@Dao
interface VideoDao {

    @Query("SELECT * from videoinfo")
    fun getVideoInfos(): List<PhotoInfo>

    @Query("SELECT * from videoinfo where id = :id")
    fun getVideoInfo(id: String): VideoInfo

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(requestInfo: VideoInfo)

    @Query("DELETE FROM videoinfo")
    suspend fun deleteAll()

    @Query("SELECT * from videoinfo")
    fun getLiveVideoInfos(): LiveData<List<VideoInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(requestInfos: List<VideoInfo>)
}