package ru.tech.easysearch.helper.client

import android.annotation.TargetApi
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.view.View.VISIBLE
import android.webkit.*
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bekawestberg.loopinglayout.library.LoopingLayoutManager
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import ru.tech.easysearch.R
import ru.tech.easysearch.activity.BrowserActivity
import ru.tech.easysearch.activity.SearchResultsActivity
import ru.tech.easysearch.adapter.toolbar.ToolbarAdapter
import ru.tech.easysearch.data.DataArrays.prefixDict


class WebClient(
    private val context: Context,
    private val toolbar: RecyclerView? = null,
    private val progressBar: LinearProgressIndicator,
    private val chromeClient: ChromeClient
) : WebViewClient() {

    val backStack: ArrayList<String> = ArrayList()

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
            if (context is BrowserActivity && temp != null) {
                val searchView: TextInputEditText = context.findViewById(R.id.searchView)
                searchView.setText(url)
            }
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
        super.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        view?.url?.let {
            backStack.add(it)
            if (context is BrowserActivity) {
                context.findViewById<TextInputEditText>(R.id.searchView).setText(it.substringAfter("host="))
                context.lastUrl = url.toString()
                context.clickedGo = false
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        var message: String? = null
        when (error?.errorCode) {
            ERROR_AUTHENTICATION -> {
                message = "User authentication failed on server"
            }
            ERROR_TIMEOUT -> {
                message = "The server is taking too much time to communicate. Try again later."
            }
            ERROR_TOO_MANY_REQUESTS -> {
                message = "Too many requests during this load"
            }
            ERROR_UNKNOWN -> {
                message = "Generic error"
            }
            ERROR_BAD_URL -> {
                message = "Check entered URL.."
            }
            ERROR_CONNECT -> {
                message = "Failed to connect to the server"
            }
            ERROR_FAILED_SSL_HANDSHAKE -> {
                message = "Failed to perform SSL handshake"
            }
            ERROR_HOST_LOOKUP -> {
                message = "Server or proxy hostname lookup failed"
            }
            ERROR_PROXY_AUTHENTICATION -> {
                message = "User authentication failed on proxy"
            }
            ERROR_REDIRECT_LOOP -> {
                message = "Too many redirects"
            }
            ERROR_UNSUPPORTED_AUTH_SCHEME -> {
                message = "Unsupported authentication scheme (not basic or digest)"
            }
            ERROR_UNSUPPORTED_SCHEME -> {
                message = "unsupported scheme"
            }
            ERROR_FILE -> {
                message = "Generic file error"
            }
            ERROR_FILE_NOT_FOUND -> {
                message = "File not found"
            }
            ERROR_IO -> {
                message = "The server failed to communicate. Try again later."
            }
        }
        view?.loadUrl("file:///android_asset/www/error_page.html?errorCode=${message}&host=${request?.url?.host}")
        chromeClient.onReceivedIcon(
            view,
            BitmapFactory.decodeResource(context.resources, R.drawable.ic_lightning)
        )
    }
}