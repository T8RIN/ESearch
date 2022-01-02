package ru.tech.easysearch.adapter.bookmark

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import ru.tech.easysearch.R
import ru.tech.easysearch.activity.BrowserActivity
import ru.tech.easysearch.application.ESearchApplication
import ru.tech.easysearch.data.BrowserTabs.createNewTab
import ru.tech.easysearch.database.bookmarks.Bookmark
import ru.tech.easysearch.databinding.BookmarkItemBinding
import ru.tech.easysearch.fragment.bookmarks.BookmarksFragment
import ru.tech.easysearch.functions.Functions
import ru.tech.easysearch.functions.Functions.byteArrayToBitmap


class BookmarksAdapter(
    private val fragment: BookmarksFragment,
    private var bookmarkList: List<Bookmark>,
    private val browser: WebView?
) :
    RecyclerView.Adapter<BookmarksAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            BookmarkItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bookmark = bookmarkList[position]
        holder.icon.setImageBitmap(byteArrayToBitmap(bookmark.icon!!))
        holder.description.text = bookmark.description
        holder.url.text = bookmark.url
        holder.itemView.setOnClickListener {
            if (browser != null) (fragment.requireActivity() as BrowserActivity).createNewTab(
                bookmark.url
            )
            else {
                val intent = Intent(fragment.requireContext(), BrowserActivity::class.java)
                intent.putExtra("url", bookmark.url)
                fragment.requireContext().startActivity(intent)
            }
            fragment.dismiss()
        }
        holder.itemView.setOnLongClickListener {
            val menu = PopupMenu(fragment.requireContext(), it)
            menu.setForceShowIcon(true)
            menu.menu.add(0, 1, 0, R.string.delete).setIcon(R.drawable.ic_baseline_delete_sweep_24)
            menu.setOnMenuItemClickListener { item ->
                if (item.itemId == 1) {
                    Functions.doInBackground {
                        ESearchApplication.database.bookmarkDao().delete(bookmark)
                    }
                }
                true
            }
            menu.show()
            true
        }
    }

    override fun getItemCount(): Int {
        return bookmarkList.size
    }

    inner class ViewHolder(binding: BookmarkItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val icon: ImageView = binding.icon
        val description: TextView = binding.description
        val url: TextView = binding.url
    }

}
