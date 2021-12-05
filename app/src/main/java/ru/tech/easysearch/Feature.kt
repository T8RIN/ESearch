package ru.tech.easysearch

import androidx.annotation.DrawableRes

data class Feature(
    @DrawableRes val iconResource: Int,
    val contentDescription: String,
)