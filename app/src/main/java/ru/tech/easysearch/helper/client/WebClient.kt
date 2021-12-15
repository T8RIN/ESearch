package ru.tech.easysearch.helper.client

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
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
    private val progressBar: LinearProgressIndicator
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
        view?.url?.let {
            backStack.add(it)
            if (context is BrowserActivity) {
                context.findViewById<TextInputEditText>(R.id.searchView).setText(it)
                context.lastUrl = url.toString()
                context.clickedGo = false
            }
        }
        super.onPageStarted(view, url, favicon)
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        view?.loadUrl("file:///android_asset/www/error.html?errorCode=${error}&errorDescription=${request?.url}");
    }
}