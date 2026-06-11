package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DownloadEntity)

    @Query("SELECT * FROM DownloadEntity WHERE id = :id")
    suspend fun get(id: String): DownloadEntity?

    @Query("SELECT * FROM DownloadEntity ORDER BY rowid DESC")
    fun getAllFlow(): Flow<List<DownloadEntity>>

    @Query("DELETE FROM DownloadEntity WHERE id = :id")
    suspend fun delete(id: String)
}
