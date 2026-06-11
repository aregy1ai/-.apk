package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DownloadEntity(
    @PrimaryKey val id: String,
    val url: String,
    val fileName: String,
    val progress: Int,
    val downloaded: Long,
    val total: Long,
    val status: DownloadStatus
)
