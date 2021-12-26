package ru.tech.easysearch.helper.client

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.view.View.VISIBLE
import android.webkit.*
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bekawestberg.loopinglayout.library.LoopingLayoutManager
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.tech.easysearch.R
import ru.tech.easysearch.activity.BrowserActivity
import ru.tech.easysearch.activity.SearchResultsActivity
import ru.tech.easysearch.adapter.toolbar.ToolbarAdapter
import ru.tech.easysearch.application.ESearchApplication.Companion.database
import ru.tech.easysearch.custom.BrowserView
import ru.tech.easysearch.data.DataArrays.prefixDict
import ru.tech.easysearch.data.SharedPreferencesAccess
import ru.tech.easysearch.data.SharedPreferencesAccess.SAVE_HISTORY
import ru.tech.easysearch.data.SharedPreferencesAccess.getSetting
import ru.tech.easysearch.database.hist.History
import ru.tech.easysearch.extensions.Extensions.fetchFavicon
import ru.tech.easysearch.extensions.Extensions.toByteArray
import ru.tech.easysearch.functions.Functions
import ru.tech.easysearch.functions.ScriptsJS.desktopScript
import ru.tech.easysearch.functions.ScriptsJS.doNotTrackScript1
import ru.tech.easysearch.functions.ScriptsJS.doNotTrackScript2
import ru.tech.easysearch.functions.ScriptsJS.doNotTrackScript3
import ru.tech.easysearch.functions.ScriptsJS.privacyScript
import java.text.DateFormatSymbols
import java.util.*


class WebClient(
    private val context: Context,
    private val toolbar: RecyclerView? = null,
    private val progressBar: LinearProgressIndicator
) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = request.url.toString()

        val currentEngine =
            prefixDict[(toolbar?.adapter as ToolbarAdapter?)?.labelList?.get((toolbar?.layoutManager as LoopingLayoutManager).findLastCompletelyVisibleItemPosition())]
        val temp = Uri.parse(url).host

        if (currentEngine?.contains(temp.toString()) == false && context is SearchResultsActivity) {
            val intent = Intent(context, BrowserActivity::class.java)
            intent.putExtra("url", url)
            context.startActivity(intent)
        } else {
            if (URLUtil.isNetworkUrl(url)) view.loadUrl(url)
            else if (url.startsWith("intent://")) {
                val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                val extraUrl = intent.getStringExtra("browser_fallback_url")
                extraUrl?.let { view.loadUrl(it) }
            } else {
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                } catch (error: ActivityNotFoundException) {
                    Toast.makeText(context, R.string.appError, Toast.LENGTH_LONG).show()
                }
            }
        }

        return true
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        progressBar.visibility = VISIBLE

        if (context is BrowserActivity) {
            context.searchView?.setText(view?.url!!)
            context.lastUrl = view?.url!!
            context.clickedGo = false
            context.iconView?.visibility = VISIBLE
            CoroutineScope(Dispatchers.Main).launch {
                val icon = getIcon(view.url!!)
                context.iconView?.setImageBitmap(icon)
            }
        }
        super.onPageStarted(view, url, favicon)
    }


    override fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {

        val adBlock = getSetting(context, SharedPreferencesAccess.AD_BLOCK)
        val imageLoading = getSetting(context, SharedPreferencesAccess.IMAGE_LOADING)
        val location = getSetting(context, SharedPreferencesAccess.LOCATION_ACCESS)
        val cookies = getSetting(context, SharedPreferencesAccess.COOKIES)
        val js = getSetting(context, SharedPreferencesAccess.JS)
        val popups = getSetting(context, SharedPreferencesAccess.POPUPS)
        val dom = getSetting(context, SharedPreferencesAccess.DOM_STORAGE)

        val manager = CookieManager.getInstance()
        if (cookies) {
            manager.setAcceptCookie(true)
            manager.getCookie(url)
        } else manager.setAcceptCookie(false)

        view.settings.apply {
            javaScriptEnabled = js
            domStorageEnabled = dom
            blockNetworkImage = !imageLoading
            javaScriptCanOpenWindowsAutomatically = popups
            setGeolocationEnabled(location)
        }


        if (!isReload && getSetting(context, SAVE_HISTORY)) {

            view.url?.let {
                if (context is BrowserActivity) {
                    context.searchView?.setText(it)
                    context.lastUrl = it
                    context.clickedGo = false

                    context.backwardBrowser?.apply {
                        when (view.canGoBack()) {
                            false -> {
                                alpha = 0.5f
                                isClickable = false
                            }
                            true -> {
                                alpha = 1f
                                isClickable = true
                            }
                        }
                    }

                    context.forwardBrowser?.apply {
                        when (view.canGoForward()) {
                            false -> {
                                alpha = 0.5f
                                isClickable = false
                            }
                            true -> {
                                alpha = 1f
                                isClickable = true
                            }
                        }
                    }

                }

                val calendar = Calendar.getInstance()
                val day = calendar[Calendar.DAY_OF_MONTH]
                val month = calendar[Calendar.MONTH]
                val year = calendar[Calendar.YEAR]
                val strHour = calendar[Calendar.HOUR_OF_DAY]
                val strMinute = calendar[Calendar.MINUTE]
                val minute = when {
                    strMinute < 10 -> "0$strMinute"
                    else -> "$strMinute"
                }
                val hour = when {
                    strHour < 10 -> "0$strHour"
                    else -> "$strHour"
                }

                val stringMonth = DateFormatSymbols(Locale.getDefault()).months[month]

                val sortingString = "$day-$month-$year | $hour:$minute"

                var title = ""
                Handler(context.mainLooper).postDelayed({
                    title = view.title!!
                    if (title.isEmpty()) title = it
                }, 900)

                Functions.delayedDoInBackground(1000) {
                    val icon = context.fetchFavicon(it).toByteArray()
                    val dao = database.historyDao()
                    dao.insert(
                        History(
                            title,
                            it,
                            icon,
                            "${hour}:${minute}",
                            "$day $stringMonth $year",
                            sortingString
                        )
                    )
                }
            }
        }

        super.doUpdateVisitedHistory(view, url, isReload)
    }

    private suspend fun getIcon(url: String): Bitmap = withContext(Dispatchers.IO) {
        return@withContext context.fetchFavicon(url)
    }

    override fun onLoadResource(view: WebView, url: String?) {
        if ((view as BrowserView).isDesktop()) view.evaluateJavascript(desktopScript, null)
        view.apply {
            evaluateJavascript(privacyScript, null)
            evaluateJavascript(doNotTrackScript1, null)
            evaluateJavascript(doNotTrackScript2, null)
            evaluateJavascript(doNotTrackScript3, null)
        }
    }
}