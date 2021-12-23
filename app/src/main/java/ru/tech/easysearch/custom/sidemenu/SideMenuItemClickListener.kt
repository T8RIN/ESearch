package ru.tech.easysearch.custom.sidemenu

class SideMenuItemClickListener(val clickListener: (SideMenuItem) -> Unit) {
    fun onClick(sideMenuItem: SideMenuItem) = clickListener(sideMenuItem)
}