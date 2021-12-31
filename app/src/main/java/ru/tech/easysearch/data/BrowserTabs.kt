package ru.tech.easysearch.data

import android.graphics.Bitmap
import android.graphics.Canvas
import ru.tech.easysearch.R
import ru.tech.easysearch.activity.BrowserActivity
import ru.tech.easysearch.activity.MainActivity.Companion.displayOffsetX
import ru.tech.easysearch.activity.MainActivity.Companion.displayOffsetY
import ru.tech.easysearch.custom.view.BrowserView
import ru.tech.easysearch.extensions.Extensions.fetchFavicon
import ru.tech.easysearch.functions.Functions
import ru.tech.easysearch.helper.client.ChromeClient
import ru.tech.easysearch.helper.client.WebClient

object BrowserTabs {

    val openedTabs: ArrayList<BrowserTabItem> = ArrayList()

    fun BrowserActivity.createNewTab(url: String = "https://google.com") {
        val container = binding.webViewContainer
        val newTab = layoutInflater.inflate(R.layout.browser_tab, container, false) as BrowserView
        container.removeView(findViewById(R.id.webBrowser))
        container.addView(newTab)

        newTab.webViewClient = WebClient(this, progressBar!!)
        newTab.webChromeClient = ChromeClient(this, progressBar!!, newTab)
        newTab.loadUrl(url)

        openedTabs.add(BrowserTabItem(null, null, newTab.title!!, url, newTab))
        browser = findViewById(R.id.webBrowser)
        while (searchView?.isFocused == true) searchView?.clearFocus()
    }

    fun BrowserActivity.saveLastTab() {
        val container = binding.webViewContainer
        val lastTab: BrowserView = openedTabs[openedTabs.lastIndex].tab

        var width = container.width
        var height = container.height

        if (width == 0) width = -displayOffsetX.toInt()
        if (height == 0) height = -displayOffsetY.toInt()
        val fullSnap =
            Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888
            )
        val canvas = Canvas(fullSnap)
        container.draw(canvas)

        val cutSnap = Bitmap.createBitmap(fullSnap, 0, 0, width, (width / 130f * 140f).toInt())

        if (openedTabs.isEmpty()) {
            openedTabs.add(
                BrowserTabItem(
                    fullSnap,
                    cutSnap,
                    lastTab.title!!,
                    lastTab.url!!,
                    lastTab
                )
            )
        } else {
            val lastTabItem = openedTabs[openedTabs.lastIndex]
            lastTabItem.fullSnap = fullSnap
            lastTabItem.cutSnap = cutSnap
            lastTabItem.title = lastTab.title!!
            lastTabItem.url = lastTab.url!!
            lastTabItem.tab = lastTab
            openedTabs[openedTabs.lastIndex] = lastTabItem
        }
    }

    fun BrowserActivity.loadTab(position: Int, save: Boolean = true) {
        val tabToLoad = openedTabs[position].tab
        val temp = openedTabs[position]
        val container = binding.webViewContainer
        openedTabs.remove(temp)
        openedTabs.add(temp)
        if (save) this.saveLastTab()
        container.removeView(findViewById(R.id.webBrowser))
        container.addView(tabToLoad)
        browser = tabToLoad
        browser?.webChromeClient = ChromeClient(this, progressBar!!, tabToLoad)
        browser?.webViewClient = WebClient(this, progressBar!!)
        this.updateBottomNav()
        val url = tabToLoad.url!!
        searchView?.setText(url)
        while (searchView?.isFocused == true) searchView?.clearFocus()
        Functions.doInIoThreadWithObservingOnMain({
            fetchFavicon(url)
        }, {
            iconView?.setImageBitmap(it as Bitmap)
        })
    }

    fun BrowserActivity.updateBottomNav() {
        backwardBrowser?.apply {
            when (browser!!.canGoBack()) {
                false -> {
                    alpha = 0.5f
                    setOnClickListener(null)
                    isClickable = false
                }
                true -> {
                    alpha = 1f
                    setOnClickListener { browser?.goBack() }
                    isClickable = true
                }
            }
        }

        forwardBrowser?.apply {
            when (browser!!.canGoForward()) {
                false -> {
                    alpha = 0.5f
                    setOnClickListener(null)
                    isClickable = false
                }
                true -> {
                    alpha = 1f
                    setOnClickListener { browser?.goForward() }
                    isClickable = true
                }
            }
        }
    }

}