package ru.tech.easysearch.custom.view

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.util.AttributeSet
import android.view.*
import android.webkit.CookieManager
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import android.webkit.WebView
import android.webkit.WebView.HitTestResult.*
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import okhttp3.internal.userAgent
import ru.tech.easysearch.R
import ru.tech.easysearch.activity.BrowserActivity
import ru.tech.easysearch.data.BrowserTabs.createNewTab
import ru.tech.easysearch.data.DataArrays.desktopUserAgentString
import ru.tech.easysearch.data.SharedPreferencesAccess
import ru.tech.easysearch.data.SharedPreferencesAccess.COOKIES
import ru.tech.easysearch.data.SharedPreferencesAccess.DOM_STORAGE
import ru.tech.easysearch.data.SharedPreferencesAccess.IMAGE_LOADING
import ru.tech.easysearch.data.SharedPreferencesAccess.JS
import ru.tech.easysearch.data.SharedPreferencesAccess.LOCATION_ACCESS
import ru.tech.easysearch.data.SharedPreferencesAccess.POPUPS
import ru.tech.easysearch.data.SharedPreferencesAccess.getSetting
import ru.tech.easysearch.extensions.Extensions.actionsForImage
import ru.tech.easysearch.extensions.Extensions.actionsForLink
import ru.tech.easysearch.extensions.Extensions.forceNightMode
import ru.tech.easysearch.extensions.Extensions.getByteArray
import ru.tech.easysearch.extensions.Extensions.hideKeyboard
import ru.tech.easysearch.extensions.Extensions.makeClip
import ru.tech.easysearch.extensions.Extensions.openEmail
import ru.tech.easysearch.extensions.Extensions.openMaps
import ru.tech.easysearch.extensions.Extensions.openPhone
import ru.tech.easysearch.extensions.Extensions.shareWith
import ru.tech.easysearch.extensions.Extensions.writeBitmap
import ru.tech.easysearch.functions.Functions.byteArrayToBitmap
import ru.tech.easysearch.functions.Functions.doInIoThreadWithObservingOnMain
import ru.tech.easysearch.functions.Functions.getNearestFileSize
import ru.tech.easysearch.helper.adblock.AdBlocker.Companion.getDomain
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
        (context as? BrowserActivity)?.registerForContextMenu(this)
        val manager = CookieManager.getInstance()
        if (getSetting(context, COOKIES)) manager.setAcceptCookie(true)
        else manager.setAcceptCookie(false)

        settings.apply {
            javaScriptEnabled = getSetting(context, JS)
            useWideViewPort = true
            loadWithOverviewMode = true
            domStorageEnabled = getSetting(context, DOM_STORAGE)
            blockNetworkImage = !getSetting(context, IMAGE_LOADING)
            allowFileAccess = true
            allowContentAccess = true
            userAgentString = userAgentString
            setGeolocationEnabled(getSetting(context, LOCATION_ACCESS))
            javaScriptCanOpenWindowsAutomatically = getSetting(context, POPUPS)
            builtInZoomControls = true
            displayZoomControls = false
        }

        if (getSetting(context, SharedPreferencesAccess.EYE_PROTECTION)) {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK))
                WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_ON)
            else forceNightMode(true)
        } else {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK))
                WebSettingsCompat.setForceDark(
                    settings,
                    WebSettingsCompat.FORCE_DARK_OFF
                )
            else forceNightMode(false)
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
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name)

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

    override fun onCreateContextMenu(menu: ContextMenu) {
        val result = hitTestResult
        result.extra?.let {
            val extra = it
            when (result.type) {
                SRC_ANCHOR_TYPE -> {
                    setupMenu(extra.getDomain(), extra, result.type, menu)
                }
                SRC_IMAGE_ANCHOR_TYPE -> {
                    val handlerThread = HandlerThread("HandlerThread")
                    handlerThread.start()
                    val handler = Handler(handlerThread.looper)
                    val message = handler.obtainMessage()
                    requestFocusNodeHref(message)
                    val url = message.data["url"] as String?
                    setupMenu(url?.getDomain().toString(), url.toString(), result.type, menu)
                }
                IMAGE_TYPE -> {
                    setupMenu(extra.getDomain(), extra, result.type, menu)
                }
                PHONE_TYPE -> context.openPhone(extra)
                EMAIL_TYPE -> context.openEmail(extra)
                GEO_TYPE -> context.openMaps(extra)
                else -> {
                    setupMenu(extra.getDomain(), extra, 0, menu)
                }
            }
        }
        super.onCreateContextMenu(menu)
    }

    private fun setupMenu(domain: String, url: String, srcAnchorType: Int, menu: ContextMenu) {
        val listener = MenuItem.OnMenuItemClickListener {
            when (it.itemId) {
                SAVE_IMAGE -> {
                    if (url.startsWith("data:")) {
                        (context as AppCompatActivity).writeBitmap(
                            byteArrayToBitmap(
                                url.substringAfter(
                                    ","
                                ).getByteArray()
                            )
                        )
                        Toast.makeText(context, R.string.saved, Toast.LENGTH_SHORT).show()
                    } else {
                        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(url)
                        val name = URLUtil.guessFileName(url, null, fileExtension)

                        val request = DownloadManager.Request(Uri.parse(url))
                            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                            .setMimeType("image/jpeg")
                            .addRequestHeader("cookie", CookieManager.getInstance().getCookie(url))
                            .addRequestHeader("User-Agent", userAgent)
                            .setTitle(name)
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            .setAllowedOverMetered(true)
                            .setAllowedOverRoaming(false)
                            .setDestinationInExternalPublicDir(
                                Environment.DIRECTORY_DOWNLOADS,
                                name
                            )

                        val downloadManager =
                            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                        downloadManager.enqueue(request)
                    }
                }
                COPY_LINK -> context.makeClip(url)
                NEW_TAB -> {
                    (context as BrowserActivity).createNewTab(url)
                }
                VIEW_IMAGE -> {
                    (context as BrowserActivity).createNewTab(url)
                }
                SHARE_LINK -> context.shareWith(url)
            }
            true
        }

        menu.setHeaderTitle(domain)
        menu.setHeaderIcon(
            ContextCompat.getDrawable(
                context,
                R.drawable.ic_baseline_insert_link_24
            )
        )

        when (srcAnchorType) {
            SRC_ANCHOR_TYPE -> menu.actionsForLink(listener)
            IMAGE_TYPE -> {
                menu.setHeaderTitle(R.string.image)
                menu.setHeaderIcon(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_baseline_image_24
                    )
                )
                menu.actionsForImage(listener)
            }
            SRC_IMAGE_ANCHOR_TYPE -> menu.actionsForLink(listener)
            else -> menu.actionsForLink(listener)
        }
    }

    fun isDesktop(): Boolean {
        return when (settings.userAgentString) {
            desktopUserAgentString -> true
            else -> false
        }
    }

    companion object {
        const val NEW_TAB = -4
        const val SAVE_IMAGE = -5
        const val VIEW_IMAGE = -6
        const val COPY_LINK = -7
        const val SHARE_LINK = -8
    }

}