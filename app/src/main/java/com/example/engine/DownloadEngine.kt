package com.example.engine

import android.content.Context
import com.example.data.DownloadEntity
import kotlinx.coroutines.*
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object DownloadEngine {
    private val tasks = ConcurrentHashMap<String, DownloadTask>()
    private var scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun start(
        context: Context,
        id: String,
        url: String,
        fileName: String,
        onUpdate: suspend (DownloadEntity) -> Unit
    ) {
        val safeName = fileName.replace("[^a-zA-Z0-9._-]".toRegex(), "_").replace(" ", "_")
        val file = File(context.getExternalFilesDir(null), safeName)
        val task = DownloadTask(id, url, file, onUpdate)
        tasks[id] = task
        task.start(scope)
    }

    fun pause(id: String) = tasks[id]?.pause()
    fun resume(id: String) = tasks[id]?.resume()

    fun cancel(id: String) {
        tasks[id]?.cancel()
        tasks.remove(id)
    }

    fun cancelAll() {
        tasks.values.forEach { it.cancel() }
        tasks.clear()
        scope.cancel()
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    }
}
