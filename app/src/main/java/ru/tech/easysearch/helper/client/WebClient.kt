package ru.tech.easysearch.helper.client

import android.graphics.Bitmap
import android.view.View.VISIBLE
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.widget.SearchView
import com.google.android.material.progressindicator.LinearProgressIndicator
import ru.tech.easysearch.data.DataArrays.prefixDict

class WebClient(
    private val searchView: SearchView,
    private val progressBar: LinearProgressIndicator
) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        view.loadUrl(request.url.toString())
        return true
    }

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        view.loadUrl(url)
        return true
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        progressBar.visibility = VISIBLE

        if (url != null) {
            var shouldSetUrl = true
            val temp = ArrayList(url.split("/"))
            val prefix = temp.let { "${it[0]}//${it[2]}" }

            for (i in prefixDict.values) {
                if (prefix.let { i.contains(it) }) {
                    shouldSetUrl = false
                    break
                }
            }
            if (shouldSetUrl) {
                searchView.setQuery(url.removePrefix("https://"), false)
            }
        }

        super.onPageStarted(view, url, favicon)
    }
}