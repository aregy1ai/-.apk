package com.example.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

class DownloadNotificationManager(private val context: Context) {
    private val manager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        val channel = NotificationChannel(
            "downloads",
            "Downloads",
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(channel)
    }

    fun buildProgress(id: Int, name: String, progress: Int, total: Long): Notification {
        return NotificationCompat.Builder(context, "downloads")
            .setContentTitle(name)
            .setContentText(if (total > 0) "$progress%" else "Downloading...")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, total <= 0)
            .setOngoing(true)
            .build()
    }

    fun show(id: Int, notification: Notification) {
        manager.notify(id, notification)
    }

    fun cancel(id: Int) {
        manager.cancel(id)
    }
}
