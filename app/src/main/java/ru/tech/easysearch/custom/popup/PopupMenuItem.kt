package ru.tech.easysearch.custom.popup

import android.graphics.drawable.Drawable

data class PopupMenuItem(
    val id: Int?,
    val icon: Drawable?,
    val title: String,
    val showSwitcher: Boolean = false,
    val showDivider: Boolean = false
)
