package com.vkpapps.sendkr.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vkpapps.sendkr.model.HistoryInfo

@Dao
interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(audioInfo: HistoryInfo)

    @Query("DELETE FROM historyinfo")
    suspend fun deleteAll()

    @Query("DELETE FROM historyinfo where id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * from historyinfo order by date DESC")
    fun getLiveHistoryInfos(): LiveData<List<HistoryInfo>>
}