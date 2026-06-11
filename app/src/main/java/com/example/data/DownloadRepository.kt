package com.example.data

import kotlinx.coroutines.flow.Flow

class DownloadRepository(private val dao: DownloadDao) {
    suspend fun save(entity: DownloadEntity) = dao.insert(entity)
    fun observeDownloads(): Flow<List<DownloadEntity>> = dao.getAllFlow()
    suspend fun get(id: String) = dao.get(id)
    suspend fun delete(id: String) = dao.delete(id)
}
