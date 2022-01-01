package ru.tech.easysearch.data

import android.graphics.Bitmap
import ru.tech.easysearch.custom.view.BrowserView

data class BrowserTabItem(
    var fullSnap: Bitmap?,
    var title: String,
    var url: String,
    var tab: BrowserView
)