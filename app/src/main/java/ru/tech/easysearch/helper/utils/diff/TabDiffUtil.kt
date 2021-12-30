package ru.tech.easysearch.helper.utils.diff

import androidx.recyclerview.widget.DiffUtil
import ru.tech.easysearch.data.BrowserTabItem

class TabDiffUtil(
    private val oldList: List<BrowserTabItem>?,
    private val newList: List<BrowserTabItem>?
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList!!.size
    }

    override fun getNewListSize(): Int {
        return newList!!.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldTab: BrowserTabItem = oldList!![oldItemPosition]
        val newTab: BrowserTabItem = newList!![newItemPosition]
        return oldTab == newTab
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldTab: BrowserTabItem = oldList!![oldItemPosition]
        val newTab: BrowserTabItem = newList!![newItemPosition]
        return (oldTab.url == newTab.url && oldTab.title == newTab.title && oldTab.fullSnap == newTab.fullSnap)
    }

}