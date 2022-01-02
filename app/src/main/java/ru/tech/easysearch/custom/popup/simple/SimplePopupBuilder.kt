package ru.tech.easysearch.custom.popup.simple

import android.content.Context
import android.view.View
import androidx.appcompat.widget.PopupMenu

class SimplePopupBuilder(context: Context, anchor: View) {

    val menu: PopupMenu
    var simplePopupClickListener = SimplePopupClickListener {}

    init {
        menu = PopupMenu(context, anchor)
        menu.setForceShowIcon(true)
        menu.setOnMenuItemClickListener {
            simplePopupClickListener.onClick(it.itemId)
            true
        }
    }

    fun addItems(vararg popupItems: SimplePopupItem): SimplePopupBuilder {
        for (item in popupItems) {
            menu.menu.add(0, item.id, 0, item.label).setIcon(item.icon)
        }
        return this
    }

    fun setMenuClickListener(listener: SimplePopupClickListener): SimplePopupBuilder {
        simplePopupClickListener = listener
        return this
    }

    fun show() {
        menu.show()
    }

    @JvmName("getMenuCustom")
    fun getMenu(): PopupMenu {
        return menu
    }

    companion object {
        const val DELETE = 101
        const val EDIT = 102
        const val SHARE = 103
        const val COPY = 104
    }

}