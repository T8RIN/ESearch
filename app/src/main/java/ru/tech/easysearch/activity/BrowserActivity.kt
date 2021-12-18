package ru.tech.easysearch.activity

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.SearchManager
import android.content.Intent
import android.content.Intent.*
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.KeyEvent
import android.view.View.GONE
import android.view.View.VISIBLE
import android.webkit.URLUtil
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import ru.tech.easysearch.R
import ru.tech.easysearch.custom.BrowserView
import ru.tech.easysearch.extensions.Extensions.hideKeyboard
import ru.tech.easysearch.fragment.bookmarks.BookmarksFragment
import ru.tech.easysearch.fragment.current.CurrentWindowsFragment
import ru.tech.easysearch.fragment.recent.RecentFragment
import ru.tech.easysearch.helper.client.ChromeClient
import ru.tech.easysearch.helper.client.WebClient

class BrowserActivity : AppCompatActivity() {

    var searchView: TextInputEditText? = null
    private var progressBar: LinearProgressIndicator? = null
    private var iconView: ImageView? = null
    private var goButton: ImageButton? = null

    private var backwardBrowser: ImageView? = null
    private var forwardBrowser: ImageButton? = null
    private var currentWindows: ImageButton? = null
    private var bookmarksBrowser: ImageButton? = null
    private var historyBrowser: ImageButton? = null

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

        backwardBrowser = findViewById(R.id.backwardBrowser)
        forwardBrowser = findViewById(R.id.forwardBrowser)
        currentWindows = findViewById(R.id.windowsBrowser)
        bookmarksBrowser = findViewById(R.id.bookmarkBrowser)
        historyBrowser = findViewById(R.id.historyBrowser)

        browser = findViewById(R.id.webBrowser)

        val chromeClient = ChromeClient(this, progressBar!!, iconView)
        browser!!.webViewClient = WebClient(this, null, progressBar!!, chromeClient)
        browser!!.webChromeClient = chromeClient

        val url = dispatchIntent(intent)

        url?.let { searchView?.let { it1 -> onGetUri(it1, it) } }

        searchView?.setOnFocusChangeListener { _, focused ->
            goButton?.visibility = when (focused) {
                true -> VISIBLE
                else -> GONE
            }
            if (!focused && !clickedGo) searchView?.setText(lastUrl)
        }

        searchView?.setOnKeyListener { _, _, keyEvent ->
            var handled = false
            if (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_DOWN) {
                onGetUri(searchView!!, searchView!!.text.toString(), false)
                handled = true
            }
            handled
        }

        goButton = findViewById(R.id.goButton)
        goButton!!.setOnClickListener {
            onGetUri(searchView!!, searchView!!.text.toString())
        }

        backwardBrowser?.setOnClickListener {
            if (browser?.canGoBack() == true) browser?.goBack()
            else Toast.makeText(this, R.string.cantGoBack, Toast.LENGTH_SHORT).show()
        }

        forwardBrowser?.setOnClickListener {
            if (browser?.canGoForward() == true) browser?.goForward()
            else Toast.makeText(this, R.string.cantGoForward, Toast.LENGTH_SHORT).show()
        }

        currentWindows?.setOnClickListener {
            CurrentWindowsFragment().show(supportFragmentManager, "custom")
        }

        bookmarksBrowser?.setOnClickListener {
            BookmarksFragment().show(supportFragmentManager, "custom")
        }

        historyBrowser?.setOnClickListener{
            RecentFragment().show(supportFragmentManager, "custom")
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

    private var browser: BrowserView? = null

    override fun onBackPressed() {
        when {
            browser?.canGoBack() == true -> {
                browser?.goBack()
            }
            else -> {
                super.onBackPressed()
                overridePendingTransition(R.anim.enter_slide_up, R.anim.exit_slide_down)
            }
        }
    }


    private fun onGetUri(it: TextInputEditText, uriLast: String, clearFocus: Boolean = true) {
        clickedGo = true
        val prefix = "https://www.google.com/search?q="
        val tempUrl = when {
            !uriLast.contains("https://") && !uriLast.contains("http://") -> "https://$uriLast"
            else -> uriLast
        }
        if (URLUtil.isValidUrl(tempUrl) && Patterns.WEB_URL.matcher(tempUrl).matches()) {
            browser?.loadUrl(tempUrl)
            lastUrl = tempUrl
        } else {
            browser?.loadUrl(prefix + uriLast)
        }

        browser?.hideKeyboard(this)

        if (clearFocus) it.clearFocus()
    }

}