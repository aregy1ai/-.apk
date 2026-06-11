package com.example.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.data.DownloadEntity
import com.example.engine.DownloadEngine
import com.example.notification.DownloadNotificationManager
import kotlinx.coroutines.*

class DownloadService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var notificationManager: DownloadNotificationManager
    private lateinit var repo: com.example.data.DownloadRepository

    override fun onCreate() {
        super.onCreate()
        val db = (application as com.example.App).db
        repo = com.example.data.DownloadRepository(db.downloadDao())
        notificationManager = DownloadNotificationManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra("url") ?: return START_NOT_STICKY
        val id = intent.getStringExtra("id") ?: return START_NOT_STICKY
        val fileName = intent.getStringExtra("fileName") ?: "file"

        startForeground(
            id.hashCode(),
            notificationManager.buildProgress(id.hashCode(), fileName, 0, -1)
        )

        DownloadEngine.start(this, id, url, fileName) { entity ->
            serviceScope.launch {
                repo.save(entity)
            }
            withContext(Dispatchers.Main) {
                notificationManager.show(
                    id.hashCode(),
                    notificationManager.buildProgress(id.hashCode(), fileName, entity.progress, entity.total)
                )
                if (entity.status == com.example.data.DownloadStatus.COMPLETED ||
                    entity.status == com.example.data.DownloadStatus.FAILED
                ) {
                    notificationManager.cancel(id.hashCode())
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        DownloadEngine.cancelAll()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
