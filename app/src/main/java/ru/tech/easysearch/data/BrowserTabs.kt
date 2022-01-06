package ru.tech.easysearch.data

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.progressindicator.LinearProgressIndicator
import ru.tech.easysearch.R
import ru.tech.easysearch.activity.BrowserActivity
import ru.tech.easysearch.activity.MainActivity.Companion.displayOffsetX
import ru.tech.easysearch.activity.MainActivity.Companion.displayOffsetY
import ru.tech.easysearch.custom.view.BrowserView
import ru.tech.easysearch.data.SharedPreferencesAccess.SAVE_TABS
import ru.tech.easysearch.data.SharedPreferencesAccess.getSetting
import ru.tech.easysearch.data.SharedPreferencesAccess.mainSharedPrefsKey
import ru.tech.easysearch.extensions.Extensions.dipToPixels
import ru.tech.easysearch.extensions.Extensions.fetchFavicon
import ru.tech.easysearch.extensions.Extensions.getBitmap
import ru.tech.easysearch.extensions.Extensions.getByteArray
import ru.tech.easysearch.extensions.Extensions.getString
import ru.tech.easysearch.extensions.Extensions.toByteArray
import ru.tech.easysearch.functions.Functions
import ru.tech.easysearch.functions.Functions.byteArrayToBitmap
import ru.tech.easysearch.functions.Functions.doInBackground
import ru.tech.easysearch.helper.client.ChromeClient
import ru.tech.easysearch.helper.client.WebClient

object BrowserTabs {

    private const val splitter = "~)_~~#*!>/"

    val openedTabs: ArrayList<BrowserTabItem> = ArrayList()

    fun BrowserActivity.createNewTab(url: String = "https://google.com") {
        val container = binding.webViewContainer
        val newTab = layoutInflater.inflate(R.layout.browser_tab, container, false) as BrowserView
        container.removeView(findViewById(R.id.webBrowser))
        container.addView(newTab)

        newTab.webViewClient = WebClient(this, progressBar!!)
        newTab.webChromeClient = ChromeClient(this, progressBar!!, newTab)
        newTab.loadUrl(url)

        openedTabs.add(BrowserTabItem(null, newTab.title!!, url, newTab))
        browser = findViewById(R.id.webBrowser)
        while (searchView?.isFocused == true) searchView?.clearFocus()

        updateTabs()
    }

    fun BrowserActivity.saveLastTab() {
        val container = binding.webViewContainer
        val lastTab: BrowserView = openedTabs[openedTabs.lastIndex].tab

        var width = container.width
        var height = container.height

        if (width == 0) width = -displayOffsetX.toInt()
        if (height == 0) height = -displayOffsetY.toInt() - dipToPixels(96f).toInt()
        val fullSnap =
            Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888
            )
        val canvas = Canvas(fullSnap)
        container.draw(canvas)

        if (openedTabs.isEmpty()) {
            openedTabs.add(
                BrowserTabItem(
                    fullSnap,
                    lastTab.title!!,
                    lastTab.url!!,
                    lastTab
                )
            )
        } else {
            val lastTabItem = openedTabs[openedTabs.lastIndex]
            lastTabItem.fullSnap = fullSnap
            lastTabItem.title = lastTab.title!!
            lastTabItem.url = when (val rll = lastTab.url) {
                null -> lastTabItem.url
                else -> rll
            }
            lastTabItem.tab = lastTab
            openedTabs[openedTabs.lastIndex] = lastTabItem
        }
        updateTabs()
    }

    fun BrowserActivity.loadTab(position: Int, save: Boolean = true) {
        if (openedTabs.isEmpty()) {
            createNewTab()
        } else {
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
            if(browser?.url == null) browser?.loadUrl(temp.url)
            this.updateBottomNav()
            val url = when (val rll = tabToLoad.url) {
                null -> openedTabs[position].url
                else -> rll
            }
            searchView?.setText(url)
            while (searchView?.isFocused == true) searchView?.clearFocus()
            Functions.doInIoThreadWithObservingOnMain({
                fetchFavicon(url)
            }, {
                iconView?.let { imageView -> Glide.with(this).load(it as Bitmap).into(imageView) }
            })
        }
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

    fun Context.updateTabs() {
        if (getSetting(this, SAVE_TABS)) {
            doInBackground {
                val sp = getSharedPreferences(mainSharedPrefsKey, Context.MODE_PRIVATE)
                val tempArray: ArrayList<String> = ArrayList()
                for (i in openedTabs.indices) {
                    if (i > openedTabs.lastIndex) break
                    val item = openedTabs[i]
                    val fullSnap = when (val snap = item.fullSnap?.toByteArray()) {
                        null -> ContextCompat.getDrawable(this, R.drawable.skeleton)!!.getBitmap()!!
                            .toByteArray()
                        else -> snap
                    }
                    tempArray.add("$i$splitter${item.title}$splitter${item.url}$splitter${fullSnap.getString()}")
                }

                sp.setTabs(tempArray.toMutableSet())
            }
        }
    }

    private fun SharedPreferences.getTabs(): MutableSet<String> {
        return getStringSet("tabsOpened", setOf())!!
    }

    private fun SharedPreferences.setTabs(tabs: MutableSet<String>) {
        edit().putStringSet("tabsOpened", tabs).apply()
    }

    fun AppCompatActivity.loadOpenedTabs(progressBar: LinearProgressIndicator? = null) {
        if (getSetting(this, SAVE_TABS)) {
            val sp = getSharedPreferences(mainSharedPrefsKey, Context.MODE_PRIVATE)
            val tempArr: ArrayList<Pair<Int, BrowserTabItem>> = ArrayList()
            for (data in sp.getTabs()) {
                val dataArr = data.split(splitter)

                val id = dataArr[0]
                val title = dataArr[1]
                val url = dataArr[2]
                val fullSnap = dataArr[3].getByteArray()

                val tab = BrowserView(this)
                tab.webChromeClient = ChromeClient(this, progressBar, tab)
                tab.webViewClient = WebClient(this, progressBar)
                tab.loadUrl(url)
                tempArr.add(
                    Pair(
                        Integer.parseInt(id),
                        BrowserTabItem(byteArrayToBitmap(fullSnap), title, url, tab)
                    )
                )
            }
            tempArr.sortBy { it.first }
            for (i in tempArr) openedTabs.add(i.second)
        }
    }

    fun Bitmap.getCutSnap(): Bitmap? {
        return if (width < height) {
            Bitmap.createBitmap(this, 0, 0, width, (width / 130f * 140f).toInt())
        } else {
            Bitmap.createBitmap(this, 0, 0, height, height)
        }
    }

}