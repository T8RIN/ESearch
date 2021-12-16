package ru.tech.easysearch.activity

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.SearchManager
import android.content.Intent
import android.content.Intent.*
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.KeyEvent
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.webkit.WebView
import android.widget.ImageButton
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
    private var goButton: ImageButton? = null

    var lastUrl = ""
    var clickedGo = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser)

        overridePendingTransition(R.anim.enter_slide_up, R.anim.exit_slide_down)

        searchView = findViewById(R.id.searchView)
        progressBar = findViewById(R.id.progressIndicator)
        iconView = findViewById(R.id.icon)

        browser = findViewById(R.id.webBrowser)
        chromeClient = ChromeClient(this, progressBar!!, iconView)
        webClient = WebClient(this, null, progressBar!!, chromeClient!!)

        browser!!.webViewClient = webClient!!
        browser!!.webChromeClient = chromeClient!!

        val settings = browser!!.settings
        settings.javaScriptEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.userAgentString = DataArrays.userAgentString
        settings.javaScriptCanOpenWindowsAutomatically = true

        val url = dispatchIntent(intent)

        url?.let { onGetUri(searchView!!, it) }

        searchView?.setText(url)

        searchView?.setOnFocusChangeListener { _, focused ->
            goButton?.visibility = when (focused) {
                true -> VISIBLE
                else -> GONE
            }
            if (!focused && !clickedGo) searchView?.setText(lastUrl)
        }

        searchView?.setOnKeyListener { _, _, keyEvent ->
            if (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_DOWN) {
                onGetUri(searchView!!, searchView!!.text.toString())
            }
            true
        }

        goButton = findViewById(R.id.goButton)
        goButton!!.setOnClickListener {
            onGetUri(searchView!!, searchView!!.text.toString())
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun dispatchIntent(intent: Intent): String? {
        return when (intent.action) {
            ACTION_VIEW -> intent.dataString
            ACTION_PROCESS_TEXT -> intent.getCharSequenceExtra(EXTRA_PROCESS_TEXT)
                .toString()
            ACTION_WEB_SEARCH -> intent.getStringExtra(SearchManager.QUERY)
            ACTION_SEND -> intent.getStringExtra(EXTRA_TEXT)
            else -> intent.extras?.get("url").toString()
        }
    }

    private var browser: WebView? = null

    override fun onBackPressed() {
        when {
            browser?.canGoBack() == true && webClient?.backStack!!.isNotEmpty() -> {
                searchView?.setText(webClient?.backStack?.last())
                webClient?.backStack?.removeLast()
                browser?.goBack()
            }
            else -> {
                super.onBackPressed()
                overridePendingTransition(R.anim.enter_slide_up, R.anim.exit_slide_down)
            }
        }
    }


    private fun onGetUri(it: TextInputEditText, uriLast: String) {
        clickedGo = true
        val prefix = "https://www.google.com/search?q="
        val tempUrl = when {
            !uriLast.contains("https://") && !uriLast.contains("http://") -> "https://$uriLast"
            else -> uriLast
        }
        if (URLUtil.isValidUrl(tempUrl) && Patterns.WEB_URL.matcher(tempUrl).matches()) {
            browser?.loadUrl(tempUrl)
        } else {
            browser?.loadUrl(prefix + uriLast)
        }
        (getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
            it.windowToken,
            0
        )
        it.clearFocus()
    }

}