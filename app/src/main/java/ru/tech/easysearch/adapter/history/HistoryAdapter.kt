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
import com.bumptech.glide.Glide
import ru.tech.easysearch.R
import ru.tech.easysearch.activity.BrowserActivity
import ru.tech.easysearch.application.ESearchApplication.Companion.database
import ru.tech.easysearch.custom.popup.simple.SimplePopupBuilder
import ru.tech.easysearch.custom.popup.simple.SimplePopupBuilder.Companion.COPY
import ru.tech.easysearch.custom.popup.simple.SimplePopupBuilder.Companion.DELETE
import ru.tech.easysearch.custom.popup.simple.SimplePopupBuilder.Companion.SHARE
import ru.tech.easysearch.custom.popup.simple.SimplePopupClickListener
import ru.tech.easysearch.custom.popup.simple.SimplePopupItem
import ru.tech.easysearch.custom.stickyheader.StickyHeaderAdapter
import ru.tech.easysearch.custom.stickyheader.StickyHeaderDecoration.Companion.HEADER
import ru.tech.easysearch.custom.stickyheader.StickyHeaderDecoration.Companion.ITEM
import ru.tech.easysearch.data.BrowserTabs.createNewTab
import ru.tech.easysearch.database.hist.History
import ru.tech.easysearch.databinding.HeaderLayoutBinding
import ru.tech.easysearch.databinding.HistItemBinding
import ru.tech.easysearch.extensions.Extensions.makeClip
import ru.tech.easysearch.extensions.Extensions.shareWith
import ru.tech.easysearch.fragment.history.HistoryFragment
import ru.tech.easysearch.functions.Functions
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
            Glide.with(fragment.requireContext()).load(byteArrayToBitmap(history.icon!!))
                .into(holder.icon)
            holder.description.text = history.description
            holder.url.text = history.url
            holder.time.text = history.time
            Log.d("dao", history.date + "  " + history.time)
            holder.itemView.setOnClickListener {
                if (browser != null) (fragment.requireActivity() as BrowserActivity).createNewTab(
                    history.url
                )
                else {
                    val intent = Intent(fragment.requireContext(), BrowserActivity::class.java)
                    intent.putExtra("url", history.url)
                    fragment.requireContext().startActivity(intent)
                }
                fragment.dismiss()
            }
            holder.itemView.setOnLongClickListener {
                SimplePopupBuilder(fragment.requireContext(), it)
                    .setMenuClickListener(SimplePopupClickListener { id ->
                        when (id) {
                            SHARE -> fragment.requireContext()
                                .shareWith(history.url)
                            COPY -> fragment.requireContext()
                                .makeClip(history.url)
                            DELETE -> Functions.doInBackground {
                                database.historyDao().delete(history)
                            }
                        }
                    })
                    .addItems(
                        SimplePopupItem(
                            SHARE,
                            R.string.share,
                            R.drawable.ic_baseline_share_24
                        ),
                        SimplePopupItem(
                            COPY,
                            R.string.copy,
                            R.drawable.ic_baseline_content_copy_24
                        ),
                        SimplePopupItem(
                            DELETE,
                            R.string.delete,
                            R.drawable.ic_baseline_delete_sweep_24
                        ),
                    )
                    .show()
                true
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
