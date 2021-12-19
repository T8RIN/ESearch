package ru.tech.easysearch.helper.client

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.View.VISIBLE
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bekawestberg.loopinglayout.library.LoopingLayoutManager
import com.google.android.material.progressindicator.LinearProgressIndicator
import ru.tech.easysearch.R
import ru.tech.easysearch.activity.BrowserActivity
import ru.tech.easysearch.activity.SearchResultsActivity
import ru.tech.easysearch.adapter.toolbar.ToolbarAdapter
import ru.tech.easysearch.application.ESearchApplication
import ru.tech.easysearch.data.DataArrays.prefixDict
import ru.tech.easysearch.database.hist.History
import ru.tech.easysearch.extensions.Extensions.getBitmap
import ru.tech.easysearch.extensions.Extensions.toByteArray
import ru.tech.easysearch.functions.Functions
import java.util.*


class WebClient(
    private val context: Context,
    private val toolbar: RecyclerView? = null,
    private val progressBar: LinearProgressIndicator,
    private val chromeClient: ChromeClient
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
        super.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        url?.let {
            if (context is BrowserActivity) {
                context.searchView?.setText(it)
                context.lastUrl = it
                context.clickedGo = false
            }

            val title = view?.title!!

            val calendar = Calendar.getInstance()
            val day = calendar[Calendar.DAY_OF_MONTH]
            val month = calendar[Calendar.MONTH]
            val year = calendar[Calendar.YEAR]
            val hour = calendar[Calendar.HOUR]
            val minute = calendar[Calendar.MINUTE]

            Functions.waitForDoInBackground(4000) {
                ESearchApplication.database.historyDao().insert(
                    History(
                        title,
                        url,
                        chromeClient.iconView?.drawable?.getBitmap()?.toByteArray(),
                        "${hour}:${minute}",
                        "$day $month $year"
                    )
                )
            }
        }
        super.onPageFinished(view, url)
    }
}