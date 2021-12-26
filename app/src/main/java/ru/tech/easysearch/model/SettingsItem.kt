package ru.tech.easysearch.model

import android.graphics.drawable.Drawable

data class SettingsItem(
    val icon: Drawable?,
    val label: String,
    val checked: Boolean,
    val key: String
)
