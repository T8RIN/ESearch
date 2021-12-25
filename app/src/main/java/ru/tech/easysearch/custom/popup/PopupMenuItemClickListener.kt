package ru.tech.easysearch.custom.popup

class PopupMenuItemClickListener(val clickListener: (PopupMenuItem) -> Unit) {
    fun onClick(popupMenuItem: PopupMenuItem) = clickListener(popupMenuItem)
}

