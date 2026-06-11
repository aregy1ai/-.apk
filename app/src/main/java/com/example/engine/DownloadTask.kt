package com.example.engine

import com.example.data.DownloadEntity
import com.example.data.DownloadStatus
import kotlinx.coroutines.*
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL

class DownloadTask(
    private val id: String,
    private val url: String,
    private val file: File,
    private val onUpdate: suspend (DownloadEntity) -> Unit
) {
    @Volatile private var isPaused = false
    @Volatile private var isCancelled = false

    fun start(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            var input: InputStream? = null
            var raf: RandomAccessFile? = null
            var conn: HttpURLConnection? = null

            try {
                val total = getSize()
                var downloaded = file.length()
                raf = RandomAccessFile(file, "rw")
                raf.setLength(total.coerceAtLeast(0))
                raf.seek(downloaded)

                conn = URL(url).openConnection() as HttpURLConnection
                conn.connectTimeout = 15000
                conn.readTimeout = 15000
                if (downloaded > 0) {
                    conn.setRequestProperty("Range", "bytes=$downloaded-")
                }

                input = conn.inputStream
                val buffer = ByteArray(32 * 1024)

                while (!isCancelled) {
                    while (isPaused && !isCancelled) delay(200)
                    val read = input.read(buffer)
                    if (read == -1) break

                    raf.write(buffer, 0, read)
                    downloaded += read

                    val progress = if (total > 0) ((downloaded * 100) / total).toInt() else 0
                    withContext(Dispatchers.Main) {
                        onUpdate(
                            DownloadEntity(
                                id, url, file.name,
                                progress, downloaded, total,
                                DownloadStatus.DOWNLOADING
                            )
                        )
                    }
                }

                if (!isCancelled) {
                    withContext(Dispatchers.Main) {
                        onUpdate(
                            DownloadEntity(
                                id, url, file.name,
                                100, downloaded, total,
                                DownloadStatus.COMPLETED
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                if (!isCancelled) {
                    withContext(Dispatchers.Main) {
                        onUpdate(
                            DownloadEntity(
                                id, url, file.name,
                                0, 0, 0,
                                DownloadStatus.FAILED
                            )
                        )
                    }
                }
            } finally {
                input?.close()
                raf?.close()
                conn?.disconnect()
            }
        }
    }

    fun pause() { isPaused = true }
    fun resume() { isPaused = false }
    fun cancel() { isCancelled = true }

    private fun getSize(): Long {
        return try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.requestMethod = "HEAD"
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            val size = conn.contentLengthLong
            conn.disconnect()
            size
        } catch (e: Exception) {
            -1L
        }
    }
}
