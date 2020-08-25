package com.vkpapps.sendkr.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vkpapps.sendkr.model.PhotoInfo

@Dao
interface PhotoDao {

    @Query("SELECT * from photoinfo")
    fun getPhotoInfos(): List<PhotoInfo>

    @Query("SELECT * from photoinfo where id = :id")
    fun getPhotoInfo(id: String): PhotoInfo

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(requestInfo: PhotoInfo)

    @Query("DELETE FROM photoinfo")
    suspend fun deleteAll()

    @Query("SELECT * from photoinfo order by lastModified DESC")
    fun getLivePhotoInfos(): LiveData<List<PhotoInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(photoInfos: List<PhotoInfo>)
}