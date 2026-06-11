package com.example.ui

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.data.DownloadEntity

class DownloadAdapter(
    private val onAction: (DownloadEntity, String) -> Unit
) : ListAdapter<DownloadEntity, DownloadAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<DownloadEntity>() {
        override fun areItemsTheSame(old: DownloadEntity, new: DownloadEntity) = old.id == new.id
        override fun areContentsTheSame(old: DownloadEntity, new: DownloadEntity) = old == new
    }

    class VH(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val layout = LinearLayout(parent.context).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(TextView(context).apply { id = android.R.id.text1; width = 400 }) // add width so buttons aren't pushed out
            addView(Button(context).apply { text = "⏸" })
            addView(Button(context).apply { text = "▶" })
            addView(Button(context).apply { text = "✕" })
        }
        return VH(layout)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        val layout = holder.itemView as LinearLayout
        val text = layout.findViewById<TextView>(android.R.id.text1)
        val pause = layout.getChildAt(1) as Button
        val resume = layout.getChildAt(2) as Button
        val cancel = layout.getChildAt(3) as Button

        val progressText = if (item.total > 0) "${item.progress}%" else "جاري..."
        text.text = "${item.fileName}  $progressText"

        pause.setOnClickListener { onAction(item, "pause") }
        resume.setOnClickListener { onAction(item, "resume") }
        cancel.setOnClickListener { onAction(item, "cancel") }
    }
}
