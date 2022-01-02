package ru.tech.easysearch.custom.popup.smart

import android.graphics.drawable.Drawable

data class SmartPopupMenuItem(
    val id: Int?,
    val icon: Drawable?,
    val title: String,
    val showSwitcher: Boolean = false,
    val showDivider: Boolean = false
)
