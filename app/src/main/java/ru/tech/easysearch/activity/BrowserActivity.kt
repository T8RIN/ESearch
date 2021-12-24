package ru.tech.easysearch.activity

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.SearchManager
import android.content.Intent
import android.content.Intent.*
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.webkit.URLUtil
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import ru.tech.easysearch.R
import ru.tech.easysearch.application.ESearchApplication.Companion.database
import ru.tech.easysearch.custom.BrowserView
import ru.tech.easysearch.custom.sidemenu.SideMenu
import ru.tech.easysearch.custom.sidemenu.SideMenuItem
import ru.tech.easysearch.database.ESearchDatabase
import ru.tech.easysearch.databinding.ActivityBrowserBinding
import ru.tech.easysearch.extensions.Extensions.hideKeyboard
import ru.tech.easysearch.extensions.Extensions.setCoeff
import ru.tech.easysearch.fragment.bookmarks.BookmarksFragment
import ru.tech.easysearch.fragment.current.CurrentWindowsFragment
import ru.tech.easysearch.fragment.history.HistoryFragment
import ru.tech.easysearch.fragment.settings.SettingsFragment
import ru.tech.easysearch.fragment.vpn.VpnFragment
import ru.tech.easysearch.helper.client.ChromeClient
import ru.tech.easysearch.helper.client.WebClient


class BrowserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBrowserBinding

    var searchView: TextInputEditText? = null
    private var progressBar: LinearProgressIndicator? = null
    var iconView: ImageView? = null

    var backwardBrowser: ImageButton? = null
    var forwardBrowser: ImageButton? = null
    private var homeBrowser: ImageButton? = null
    private var currentWindows: ImageButton? = null
    private var profileBrowser: ImageButton? = null

    var lastUrl = ""
    var clickedGo = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_ESearch)

        binding = ActivityBrowserBinding.inflate(layoutInflater)
        database = ESearchDatabase.getInstance(this)
        setCoeff()

        setContentView(binding.root)

        overridePendingTransition(R.anim.enter_slide_up, R.anim.exit_slide_down)

        searchView = binding.searchView
        progressBar = binding.progressIndicator
        iconView = binding.icon
        homeBrowser = binding.homeBrowser
        backwardBrowser = binding.backwardBrowser
        forwardBrowser = binding.forwardBrowser
        profileBrowser = binding.profileBrowser
        currentWindows = binding.windowsBrowser

        browser = binding.webBrowser

        val chromeClient = ChromeClient(this, progressBar!!)
        browser!!.webViewClient = WebClient(this, null, progressBar!!)
        browser!!.webChromeClient = chromeClient

        val url = dispatchIntent(intent)

        searchView?.setText(url)

        url?.let { searchView?.let { it1 -> onGetUri(it1, it) } }

        binding.goMoreButton.inAnimation = AnimationUtils.loadAnimation(
            this,
            R.anim.fade_in
        )
        binding.goMoreButton.outAnimation = AnimationUtils.loadAnimation(
            this,
            R.anim.fade_out
        )

        searchView?.setOnFocusChangeListener { _, focused ->
            when (focused) {
                true -> {
                    binding.goMoreButton.showNext()
                    binding.goMoreButton.setOnClickListener {
                        onGetUri(searchView!!, searchView!!.text.toString())
                    }
                }
                false -> {
                    binding.goMoreButton.showPrevious()
                    binding.goMoreButton.setOnClickListener {
                        showMore()
                    }
                }
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

        backwardBrowser?.setOnClickListener {
            if (browser?.canGoBack() == true) browser?.goBack()
        }

        forwardBrowser?.setOnClickListener {
            if (browser?.canGoForward() == true) browser?.goForward()
        }

        homeBrowser?.setOnClickListener {
            finish()
            startActivity(Intent(this, MainActivity::class.java))
        }

        currentWindows?.setOnClickListener {
            CurrentWindowsFragment().show(supportFragmentManager, "custom")
        }

        binding.goMoreButton.setOnClickListener { showMore() }

        profileBrowser?.setOnClickListener {
            builder = SideMenu(binding.root.parent as ViewGroup, this)
                .setMenuItemClickListener { menuItem ->
                    when (menuItem.id) {
                        R.drawable.ic_baseline_history_24 -> {
                            HistoryFragment(browser).show(supportFragmentManager, "custom")
                        }
                        R.drawable.ic_baseline_bookmarks_24 -> {
                            BookmarksFragment(browser).show(supportFragmentManager, "custom")
                        }
                        R.drawable.ic_baseline_vpn_lock_24 -> {
                            VpnFragment().show(supportFragmentManager, "custom")
                        }
                        R.drawable.ic_baseline_settings_24 -> {
                            SettingsFragment().show(supportFragmentManager, "custom")
                        }
                    }
                    builder?.dismiss()
                }
                .addItems(
                    SideMenuItem(
                        R.drawable.ic_baseline_history_24,
                        ContextCompat.getDrawable(this, R.drawable.ic_baseline_history_24)!!,
                        getString(R.string.history)
                    ),
                    SideMenuItem(
                        R.drawable.ic_baseline_bookmarks_24,
                        ContextCompat.getDrawable(this, R.drawable.ic_baseline_bookmarks_24)!!,
                        getString(R.string.bookmarks)
                    ),
                    SideMenuItem(
                        R.drawable.ic_baseline_settings_24,
                        ContextCompat.getDrawable(this, R.drawable.ic_baseline_settings_24)!!,
                        getString(R.string.settings)
                    ),
                    SideMenuItem(
                        R.drawable.ic_baseline_vpn_lock_24,
                        ContextCompat.getDrawable(this, R.drawable.ic_baseline_vpn_lock_24)!!,
                        getString(R.string.vpn)
                    )
                )
            builder!!.show()
        }

//        binding.bookmarkButton.setOnClickListener {
//            val bookmarkDialog = BookmarkCreationDialog(lastUrl, browser?.title!!)
//            if (!bookmarkDialog.isAdded) bookmarkDialog.show(supportFragmentManager, "bookDialog")
//        }
//
//        binding.refreshButton.setOnClickListener {
//            browser?.reload()
//        }

    }

    private fun showMore() {

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
            browser?.canGoBack() == true && (builder?.isHidden == true || builder == null) -> {
                browser?.goBack()
            }
            builder?.isHidden == false -> {
                builder?.dismiss()
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

    private var builder: SideMenu? = null

    override fun onConfigurationChanged(newConfig: Configuration) {
        builder?.dismiss()
        super.onConfigurationChanged(newConfig)
    }

}