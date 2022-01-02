package ru.tech.easysearch.model

import android.graphics.drawable.Drawable

data class SettingsItem(
    val icon: Drawable? = null,
    val label: String = "",
    val checked: Boolean = true,
    val key: String = ""
)
