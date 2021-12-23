package ru.tech.easysearch.custom.stickyheader

import androidx.recyclerview.widget.RecyclerView
import ru.tech.easysearch.helper.interfaces.StickyHeaderInterface

abstract class StickyHeaderAdapter(
    private var itemsList: List<*>,
    private val booleanArray: List<Boolean>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), StickyHeaderInterface {

    override fun getItemViewType(position: Int): Int {
        if (booleanArray[position]) return StickyHeaderDecoration.HEADER
        return StickyHeaderDecoration.ITEM
    }

    override fun getItemCount(): Int {
        return itemsList.size
    }

    override fun isHeader(itemPosition: Int): Boolean {
        if (itemPosition < booleanArray.size) return booleanArray[itemPosition]
        return false
    }

    override fun getHeaderPositionForItem(itemPosition: Int): Int {
        var tempPos = itemPosition
        var headerPosition = 0
        do {
            if (isHeader(tempPos)) {
                headerPosition = tempPos
                break
            }
            tempPos -= 1
        } while (tempPos >= 0)
        return headerPosition
    }

}