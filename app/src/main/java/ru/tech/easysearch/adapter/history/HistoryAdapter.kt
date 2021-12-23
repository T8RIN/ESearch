package ru.tech.easysearch.adapter.history

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.tech.easysearch.R
import ru.tech.easysearch.activity.BrowserActivity
import ru.tech.easysearch.custom.StickyHeaderAdapter
import ru.tech.easysearch.custom.StickyHeaderDecoration.Companion.HEADER
import ru.tech.easysearch.custom.StickyHeaderDecoration.Companion.ITEM
import ru.tech.easysearch.database.hist.History
import ru.tech.easysearch.databinding.HeaderLayoutBinding
import ru.tech.easysearch.databinding.HistItemBinding
import ru.tech.easysearch.fragment.history.HistoryFragment
import ru.tech.easysearch.functions.Functions.byteArrayToBitmap


class HistoryAdapter(
    private val fragment: HistoryFragment,
    private val historyList: List<History>,
    booleanArray: List<Boolean>,
    private val browser: WebView?
) : StickyHeaderAdapter(historyList, booleanArray) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            HEADER -> {
                HeaderViewHolder(
                    HeaderLayoutBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            ITEM -> {
                HistoryViewHolder(
                    HistItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val history = historyList[position]
        if (holder is HistoryViewHolder) {
            holder.icon.setImageBitmap(history.icon?.let { byteArrayToBitmap(it) })
            holder.description.text = history.description
            holder.url.text = history.url
            holder.time.text = history.time
            Log.d("dao", history.date + "  " + history.time)
            holder.itemView.setOnClickListener {
                if (browser != null) browser.loadUrl(history.url)
                else {
                    val intent = Intent(fragment.requireContext(), BrowserActivity::class.java)
                    intent.putExtra("url", history.url)
                    fragment.requireContext().startActivity(intent)
                }
                fragment.dismiss()
            }
        } else if (holder is HeaderViewHolder) {
            holder.text.text = history.date
        }
    }

    inner class HistoryViewHolder(binding: HistItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val icon: ImageView = binding.icon
        val description: TextView = binding.description
        val url: TextView = binding.url
        val time: TextView = binding.time
    }

    inner class HeaderViewHolder(binding: HeaderLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val text: TextView = binding.text
    }

    override fun bindHeaderData(header: View?, headerPosition: Int) {
        header?.findViewById<TextView>(R.id.text)?.text = historyList[headerPosition].date
    }

}
