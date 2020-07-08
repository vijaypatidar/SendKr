package com.vkpapps.thunder.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vkpapps.thunder.model.RequestInfo

@Dao
interface RequestDao {

    @Query("SELECT * from requestinfo")
    fun getRequestInfos(): List<RequestInfo>

    @Query("SELECT * from requestinfo where rid = :rid")
    fun getRequestInfo(rid: String): RequestInfo

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(requestInfo: RequestInfo)

    @Query("DELETE FROM requestinfo")
    suspend fun deleteAll()

    @Query("SELECT * from requestinfo")
    fun getLiveRequestInfos(): LiveData<List<RequestInfo>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(requestInfos: List<RequestInfo>)
}