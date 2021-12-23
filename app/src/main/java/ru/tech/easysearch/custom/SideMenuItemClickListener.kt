package ru.tech.easysearch.custom

class SideMenuItemClickListener(val clickListener: (SideMenuItem) -> Unit) {
    fun onClick(sideMenuItem: SideMenuItem) = clickListener(sideMenuItem)
}