package ru.tech.easysearch.helper.client

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.View.VISIBLE
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
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

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = request.url.toString()

        val currentEngine =
            prefixDict[(toolbar?.adapter as ToolbarAdapter?)?.labelList?.get((toolbar?.layoutManager as LoopingLayoutManager).findLastCompletelyVisibleItemPosition())]
        val temp = Uri.parse(url).host!!

        if (currentEngine?.contains(temp) == false && context is SearchResultsActivity) {
            val intent = Intent(context, BrowserActivity::class.java)
            intent.putExtra("url", url)
            context.startActivity(intent)
        } else {
            if(context is BrowserActivity){
                val searchView: TextInputEditText = context.findViewById(R.id.searchView)
                searchView.setText(url)
            }
            view.loadUrl(url)
        }
        return true
    }


    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        progressBar.visibility = VISIBLE
        super.onPageStarted(view, url, favicon)
    }
}