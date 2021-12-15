package ru.tech.easysearch.helper.client

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.ImageView
import com.google.android.material.progressindicator.LinearProgressIndicator

class ChromeClient(
    private val context: Context,
    private val progressBar: LinearProgressIndicator,
    private var iconView: ImageView? = null
) :
    WebChromeClient() {

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        progressBar.progress = newProgress
        if (newProgress == 100) {
            progressBar.visibility = View.GONE
        }
    }

    override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
        iconView?.visibility = View.VISIBLE
        iconView?.setImageBitmap(icon)
        super.onReceivedIcon(view, icon)
    }

}
