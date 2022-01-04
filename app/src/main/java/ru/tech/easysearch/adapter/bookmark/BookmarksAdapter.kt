package ru.tech.easysearch.adapter.bookmark

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import ru.tech.easysearch.R
import ru.tech.easysearch.activity.BrowserActivity
import ru.tech.easysearch.application.ESearchApplication.Companion.database
import ru.tech.easysearch.custom.popup.simple.SimplePopupBuilder
import ru.tech.easysearch.custom.popup.simple.SimplePopupBuilder.Companion.COPY
import ru.tech.easysearch.custom.popup.simple.SimplePopupBuilder.Companion.DELETE
import ru.tech.easysearch.custom.popup.simple.SimplePopupBuilder.Companion.EDIT
import ru.tech.easysearch.custom.popup.simple.SimplePopupBuilder.Companion.SHARE
import ru.tech.easysearch.custom.popup.simple.SimplePopupClickListener
import ru.tech.easysearch.custom.popup.simple.SimplePopupItem
import ru.tech.easysearch.data.BrowserTabs.createNewTab
import ru.tech.easysearch.database.bookmarks.Bookmark
import ru.tech.easysearch.databinding.BookmarkItemBinding
import ru.tech.easysearch.extensions.Extensions.getAttrColor
import ru.tech.easysearch.extensions.Extensions.makeClip
import ru.tech.easysearch.extensions.Extensions.shareWith
import ru.tech.easysearch.fragment.bookmarks.BookmarksFragment
import ru.tech.easysearch.fragment.dialog.BookmarkCreationDialog
import ru.tech.easysearch.functions.Functions.byteArrayToBitmap
import ru.tech.easysearch.functions.Functions.doInBackground


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
        Glide.with(fragment.requireContext()).load(byteArrayToBitmap(bookmark.icon!!))
            .into(holder.icon)
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
            SimplePopupBuilder(fragment.requireContext(), it)
                .setMenuClickListener(SimplePopupClickListener { id ->
                    when (id) {
                        EDIT -> {
                            val bookmarkEdit =
                                BookmarkCreationDialog(
                                    bookmark.url,
                                    bookmark.description,
                                    true,
                                    bookmark.id
                                )
                            if (!bookmarkEdit.isAdded) bookmarkEdit.show(
                                fragment.requireActivity().supportFragmentManager,
                                "bookmark_edition"
                            )
                        }
                        SHARE -> fragment.requireContext().shareWith(bookmark.url)
                        COPY -> fragment.requireContext().makeClip(bookmark.url)
                        DELETE -> {
                            Snackbar.make(
                                fragment.requireView(),
                                bookmark.url,
                                Snackbar.LENGTH_LONG
                            )
                                .setBackgroundTint(
                                    ContextCompat.getColor(
                                        fragment.requireContext(),
                                        R.color.materialGray
                                    )
                                )
                                .setAction(R.string.undo) {
                                    doInBackground { database.bookmarkDao().insert(bookmark) }
                                }
                                .setTextColor(
                                    ContextCompat.getColor(
                                        fragment.requireContext(),
                                        R.color.white
                                    )
                                )
                                .setActionTextColor(
                                    fragment.requireContext().getAttrColor(R.attr.colorSecondary)
                                )
                                .show()
                            doInBackground {
                                database.bookmarkDao().delete(bookmark)
                            }
                        }
                    }
                })
                .addItems(
                    SimplePopupItem(EDIT, R.string.edit, R.drawable.ic_baseline_edit_24),
                    SimplePopupItem(SHARE, R.string.share, R.drawable.ic_baseline_share_24),
                    SimplePopupItem(COPY, R.string.copy, R.drawable.ic_baseline_content_copy_24),
                    SimplePopupItem(
                        DELETE,
                        R.string.delete,
                        R.drawable.ic_baseline_delete_sweep_24
                    ),
                )
                .show()
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
