package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.service.DownloadService
import com.example.ui.DownloadsActivity
import java.util.UUID

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }
        val urlInput = EditText(this).apply { hint = "رابط التحميل" }
        val startBtn = Button(this).apply { text = "تحميل" }
        val listBtn = Button(this).apply { text = "التنزيلات" }

        layout.addView(urlInput)
        layout.addView(startBtn)
        layout.addView(listBtn)
        setContentView(layout)

        startBtn.setOnClickListener {
            val url = urlInput.text.toString().trim()
            if (url.isNotEmpty()) {
                val id = UUID.randomUUID().toString()
                val fileName = url.substringAfterLast('/').ifEmpty { "file" }
                val intent = Intent(this, DownloadService::class.java).apply {
                    putExtra("url", url)
                    putExtra("id", id)
                    putExtra("fileName", fileName)
                }
                startService(intent)
            }
        }

        listBtn.setOnClickListener {
            startActivity(Intent(this, DownloadsActivity::class.java))
        }
    }
}
