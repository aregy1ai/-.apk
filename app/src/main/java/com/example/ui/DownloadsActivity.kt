package com.example.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.data.DownloadRepository
import com.example.engine.DownloadEngine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DownloadsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@DownloadsActivity)
        }
        val emptyView = TextView(this).apply {
            text = "No downloads"
            visibility = View.GONE
        }

        val repo = DownloadRepository((application as com.example.App).db.downloadDao())
        val adapter = DownloadAdapter { entity, action ->
            when (action) {
                "pause" -> DownloadEngine.pause(entity.id)
                "resume" -> DownloadEngine.resume(entity.id)
                "cancel" -> {
                    DownloadEngine.cancel(entity.id)
                    lifecycleScope.launch { repo.delete(entity.id) }
                }
            }
        }

        recyclerView.adapter = adapter
        val layout = android.widget.FrameLayout(this).apply {
            addView(recyclerView)
            addView(emptyView)
        }
        setContentView(layout)

        lifecycleScope.launch {
            repo.observeDownloads().collectLatest { list ->
                adapter.submitList(list)
                emptyView.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
}
