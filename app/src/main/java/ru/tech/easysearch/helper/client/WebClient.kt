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
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.tech.easysearch.R
import ru.tech.easysearch.activity.BrowserActivity
import ru.tech.easysearch.activity.MainActivity
import ru.tech.easysearch.application.ESearchApplication.Companion.database
import ru.tech.easysearch.data.BrowserTabs.updateBottomNav
import ru.tech.easysearch.data.SharedPreferencesAccess.AD_BLOCK
import ru.tech.easysearch.data.SharedPreferencesAccess.COOKIES
import ru.tech.easysearch.data.SharedPreferencesAccess.DOM_STORAGE
import ru.tech.easysearch.data.SharedPreferencesAccess.EYE_PROTECTION
import ru.tech.easysearch.data.SharedPreferencesAccess.GET
import ru.tech.easysearch.data.SharedPreferencesAccess.IMAGE_LOADING
import ru.tech.easysearch.data.SharedPreferencesAccess.JS
import ru.tech.easysearch.data.SharedPreferencesAccess.LOCATION_ACCESS
import ru.tech.easysearch.data.SharedPreferencesAccess.POPUPS
import ru.tech.easysearch.data.SharedPreferencesAccess.SAVE_HISTORY
import ru.tech.easysearch.data.SharedPreferencesAccess.getSetting
import ru.tech.easysearch.data.SharedPreferencesAccess.needToChangeBrowserSettings
import ru.tech.easysearch.database.hist.History
import ru.tech.easysearch.extensions.Extensions.fetchFavicon
import ru.tech.easysearch.extensions.Extensions.forceNightMode
import ru.tech.easysearch.extensions.Extensions.isDesktop
import ru.tech.easysearch.extensions.Extensions.toByteArray
import ru.tech.easysearch.functions.Functions
import ru.tech.easysearch.functions.ScriptsJS.desktopScript
import ru.tech.easysearch.functions.ScriptsJS.doNotTrackScript1
import ru.tech.easysearch.functions.ScriptsJS.doNotTrackScript2
import ru.tech.easysearch.functions.ScriptsJS.doNotTrackScript3
import ru.tech.easysearch.functions.ScriptsJS.privacyScript
import ru.tech.easysearch.helper.adblock.AdBlocker.Companion.areAD
import java.io.ByteArrayInputStream
import java.text.DateFormatSymbols
import java.util.*

class WebClient(
    private val context: Context,
    private val progressBar: LinearProgressIndicator?
) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = request.url.toString()

        val headers: HashMap<String, String> = HashMap()
        headers["DNT"] = "1"
        headers["Sec-GPC"] = "1"
        headers["X-Requested-With"] = "com.duckduckgo.mobile.android"

        if (context is MainActivity) {
            val intent = Intent(context, BrowserActivity::class.java)
            intent.putExtra("url", url)
            context.startActivity(intent)
        } else {
            if (URLUtil.isNetworkUrl(url)) view.loadUrl(url, headers)
            else if (url.startsWith("intent://")) {
                val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                val extraUrl = intent.getStringExtra("browser_fallback_url")
                extraUrl?.let { view.loadUrl(it, headers) }
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
        progressBar?.visibility = VISIBLE

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

        if (needToChangeBrowserSettings(context, GET)) {
            val manager = CookieManager.getInstance()
            if (getSetting(context, COOKIES)) {
                manager.setAcceptCookie(true)
                manager.getCookie(url)
            } else manager.setAcceptCookie(false)

            view.settings.apply {
                javaScriptEnabled = getSetting(context, JS)
                domStorageEnabled = getSetting(context, DOM_STORAGE)
                blockNetworkImage = !getSetting(context, IMAGE_LOADING)
                javaScriptCanOpenWindowsAutomatically = getSetting(context, POPUPS)
                setGeolocationEnabled(getSetting(context, LOCATION_ACCESS))
            }

            if (getSetting(context, EYE_PROTECTION)) {
                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK))
                    WebSettingsCompat.setForceDark(view.settings, WebSettingsCompat.FORCE_DARK_ON)
                else view.forceNightMode(true)
            } else {
                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK))
                    WebSettingsCompat.setForceDark(
                        view.settings,
                        WebSettingsCompat.FORCE_DARK_OFF
                    )
                else view.forceNightMode(false)
            }
        }

        if (!isReload && getSetting(context, SAVE_HISTORY)) {

            view.url?.let {
                if (context is BrowserActivity) {
                    context.searchView?.setText(it)
                    context.lastUrl = it
                    context.clickedGo = false

                    context.updateBottomNav()
                }

                val calendar = Calendar.getInstance()
                val strDay = calendar[Calendar.DAY_OF_MONTH]
                val strMonth = calendar[Calendar.MONTH]
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

                val day = when {
                    strDay < 10 -> "0$strDay"
                    else -> "$strDay"
                }
                val month = when {
                    strMonth + 1 < 10 -> "0${strMonth + 1}"
                    else -> "${strMonth + 1}"
                }

                val stringMonth = DateFormatSymbols(Locale.getDefault()).months[strMonth]

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
                            "$strDay $stringMonth $year",
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
        if (view.isDesktop()) view.evaluateJavascript(desktopScript, null)
        view.apply {
            evaluateJavascript(privacyScript, null)
            evaluateJavascript(doNotTrackScript1, null)
            evaluateJavascript(doNotTrackScript2, null)
            evaluateJavascript(doNotTrackScript3, null)
        }
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest
    ): WebResourceResponse? {
        if (request.url.toString().areAD() && getSetting(context, AD_BLOCK)) {
            return WebResourceResponse(
                "text/plain",
                "utf-8",
                ByteArrayInputStream("".toByteArray())
            )
        }
        return super.shouldInterceptRequest(view, request)
    }
}