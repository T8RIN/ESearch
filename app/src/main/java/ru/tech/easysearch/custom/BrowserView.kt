package ru.tech.easysearch.custom

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebView
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import ru.tech.easysearch.R
import ru.tech.easysearch.data.DataArrays.desktopUserAgentString
import ru.tech.easysearch.data.SharedPreferencesAccess.AD_BLOCK
import ru.tech.easysearch.data.SharedPreferencesAccess.COOKIES
import ru.tech.easysearch.data.SharedPreferencesAccess.DOM_STORAGE
import ru.tech.easysearch.data.SharedPreferencesAccess.IMAGE_LOADING
import ru.tech.easysearch.data.SharedPreferencesAccess.JS
import ru.tech.easysearch.data.SharedPreferencesAccess.LOCATION_ACCESS
import ru.tech.easysearch.data.SharedPreferencesAccess.POPUPS
import ru.tech.easysearch.data.SharedPreferencesAccess.getSetting
import ru.tech.easysearch.extensions.Extensions.hideKeyboard
import ru.tech.easysearch.functions.Functions.doInIoThreadWithObservingOnMain
import ru.tech.easysearch.functions.Functions.getNearestFileSize
import java.net.URL
import java.net.URLConnection

@SuppressLint("SetJavaScriptEnabled", "SetTextI18n")
class BrowserView : WebView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private var rootGroup: ViewGroup? = null
    private var findBox: View? = null
    private var close: ImageButton? = null
    private var up: ImageButton? = null
    private var down: ImageButton? = null
    private var numMatches: TextView? = null
    private var searchView: TextInputEditText? = null

    init {
        val adBlock = getSetting(context, AD_BLOCK)
        val imageLoading = getSetting(context, IMAGE_LOADING)
        val location = getSetting(context, LOCATION_ACCESS)
        val cookies = getSetting(context, COOKIES)
        val js = getSetting(context, JS)
        val popups = getSetting(context, POPUPS)
        val dom = getSetting(context, DOM_STORAGE)

        val manager = CookieManager.getInstance()
        if (cookies) manager.setAcceptCookie(true)
        else manager.setAcceptCookie(false)

        settings.apply {
            javaScriptEnabled = js
            useWideViewPort = true
            loadWithOverviewMode = true
            domStorageEnabled = dom
            blockNetworkImage = !imageLoading
            allowFileAccess = true
            allowContentAccess = true
            userAgentString = userAgentString
            setGeolocationEnabled(location)
            javaScriptCanOpenWindowsAutomatically = popups
            builtInZoomControls = true
            displayZoomControls = false
            setSupportMultipleWindows(true)
        }


        setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            val name = URLUtil.guessFileName(url, contentDisposition, mimeType)

            val request = DownloadManager.Request(Uri.parse(url))
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setMimeType(mimeType)
                .addRequestHeader("cookie", CookieManager.getInstance().getCookie(url))
                .addRequestHeader("User-Agent", userAgent)
                .setTitle(name)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(false)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    URLUtil.guessFileName(url, contentDisposition, mimeType)
                )
            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

            val dialog = MaterialAlertDialogBuilder(context)
                .setTitle(R.string.download)
                .setPositiveButton(R.string.ok_ok) { _, _ ->
                    downloadManager.enqueue(request)
                    Toast.makeText(
                        context.applicationContext,
                        R.string.downloading,
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
                .setNegativeButton(R.string.cancel, null)

            doInIoThreadWithObservingOnMain({
                val urlConnection: URLConnection = URL(url).openConnection()
                urlConnection.connect()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) urlConnection.contentLengthLong
                else urlConnection.contentLength.toLong()
            }, {
                dialog.setMessage("$name\n\n${getNearestFileSize(it as Long)}")
                dialog.show()
            })

        }

        setFindListener { _, numberOfMatches, _ ->
            if (numberOfMatches != 0) {
                findArray = List(numberOfMatches) { 0 }
                numMatches?.text = "$currentIndex/$numberOfMatches"
                numMatches?.setTextColor(color)
            } else {
                findArray = List(numberOfMatches) { 0 }
                numMatches?.text = "0/0"
                numMatches?.setTextColor(ContextCompat.getColor(context, R.color.red))
            }
        }
    }

    private var color = 0
    private var findArray: List<Int> = List(0) { 0 }
    private var currentIndex = 1

    fun prepareFinding(root: ViewGroup) {
        rootGroup = root
        isFinding = true

        findBox =
            LayoutInflater.from(context).inflate(R.layout.find_in_page_layout, rootGroup, false)
        close = findBox!!.findViewById(R.id.close)
        up = findBox!!.findViewById(R.id.up)
        down = findBox!!.findViewById(R.id.down)
        numMatches = findBox!!.findViewById(R.id.numMatches)
        searchView = findBox!!.findViewById(R.id.findText)

        color = numMatches!!.currentTextColor

        rootGroup!!.addView(findBox)
        close!!.setOnClickListener {
            cancelFinding()
        }
        up!!.setOnClickListener {
            findNext(false)
        }
        down!!.setOnClickListener {
            findNext(true)
        }
        searchView!!.addTextChangedListener {
            findAllAsync(it.toString())
        }
    }

    var isFinding = false

    fun cancelFinding() {
        clearMatches()
        findBox?.clearFocus()
        rootGroup?.removeView(findBox)
        isFinding = false
    }

    override fun findNext(forward: Boolean) {
        super.findNext(forward)
        val last = findArray.size
        if (last != 0) {
            hideKeyboard(context)
            when (forward) {
                true -> {
                    currentIndex++
                    if (currentIndex > last) currentIndex = 1
                    numMatches?.text = "$currentIndex/$last"
                }
                else -> {
                    currentIndex--
                    if (currentIndex == 0) currentIndex = last
                    numMatches?.text = "$currentIndex/$last"
                }
            }
        }
    }

    fun isDesktop(): Boolean {
        return when (settings.userAgentString) {
            desktopUserAgentString -> true
            else -> false
        }
    }

}