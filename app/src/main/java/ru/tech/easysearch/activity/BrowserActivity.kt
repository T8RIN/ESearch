package ru.tech.easysearch.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.webkit.URLUtil
import android.webkit.WebView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import ru.tech.easysearch.R
import ru.tech.easysearch.data.DataArrays
import ru.tech.easysearch.helper.client.ChromeClient
import ru.tech.easysearch.helper.client.WebClient

class BrowserActivity : AppCompatActivity() {

    private var searchView: TextInputEditText? = null
    private var progressBar: LinearProgressIndicator? = null
    private var iconView: ImageView? = null
    private var webClient: WebClient? = null
    private var chromeClient: ChromeClient? = null


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser)

        overridePendingTransition(R.anim.enter_slide_up, R.anim.exit_slide_down)

        searchView = findViewById(R.id.searchView)
        progressBar = findViewById(R.id.progressIndicator)
        iconView = findViewById(R.id.icon)

        browser = findViewById(R.id.webBrowser)
        webClient = WebClient(this, null, progressBar!!)
        chromeClient = ChromeClient(this, progressBar!!, iconView)

        browser!!.webViewClient = webClient!!
        browser!!.webChromeClient = chromeClient!!

        val settings = browser!!.settings
        settings.javaScriptEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.supportMultipleWindows()
        settings.userAgentString = DataArrays.userAgentString

        val url = when (intent.action) {
            Intent.ACTION_VIEW -> intent.dataString
            else -> intent.extras?.get("url").toString()
        }

        url?.let { onGetUri(it) }

        searchView?.setText(url)
        searchView?.setSelection(0)

    }

    private var browser: WebView? = null

    override fun onBackPressed() {
        when {
            browser?.canGoBack() == true -> {
                searchView?.setText(chromeClient?.backStack?.last())
                chromeClient?.backStack?.removeLast()
                browser?.goBack()
            }
            else -> {
                super.onBackPressed()
                overridePendingTransition(R.anim.enter_slide_up, R.anim.exit_slide_down)
            }
        }
    }


    private fun onGetUri(uriLast: String) {
        this.uriLast = uriLast
        val tempUrl = when {
            !uriLast.contains("https://") && !uriLast.contains("http://") -> "https://$uriLast"
            else -> uriLast
        }
        if (URLUtil.isValidUrl(tempUrl) && Patterns.WEB_URL.matcher(tempUrl).matches()) {
            browser?.loadUrl(tempUrl)
        }

    }

    private var uriLast: String = ""

}