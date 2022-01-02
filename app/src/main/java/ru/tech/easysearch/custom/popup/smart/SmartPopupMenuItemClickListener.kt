package ru.tech.easysearch.custom.popup.smart

class SmartPopupMenuItemClickListener(val clickListener: (SmartPopupMenuItem) -> Unit) {
    fun onClick(smartPopupMenuItem: SmartPopupMenuItem) = clickListener(smartPopupMenuItem)
}

