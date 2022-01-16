package ru.tech.easysearch.activity

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.DownloadManager
import android.app.SearchManager
import android.content.Intent
import android.content.Intent.*
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.KeyEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageSwitcher
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import ru.tech.easysearch.R
import ru.tech.easysearch.application.ESearchApplication.Companion.database
import ru.tech.easysearch.custom.popup.smart.SmartPopupMenu
import ru.tech.easysearch.custom.sidemenu.SideMenu
import ru.tech.easysearch.custom.view.BrowserView
import ru.tech.easysearch.data.BrowserTabs.createNewTab
import ru.tech.easysearch.data.BrowserTabs.loadOpenedTabs
import ru.tech.easysearch.data.BrowserTabs.loadTab
import ru.tech.easysearch.data.BrowserTabs.openedTabs
import ru.tech.easysearch.data.BrowserTabs.saveLastTab
import ru.tech.easysearch.data.BrowserTabs.updateGestures
import ru.tech.easysearch.data.BrowserTabs.updateTabs
import ru.tech.easysearch.data.DataArrays
import ru.tech.easysearch.data.DataArrays.translateSite
import ru.tech.easysearch.data.SharedPreferencesAccess.HIDE_PANELS
import ru.tech.easysearch.data.SharedPreferencesAccess.getSetting
import ru.tech.easysearch.data.SharedPreferencesAccess.loadTheme
import ru.tech.easysearch.database.ESearchDatabase
import ru.tech.easysearch.extensions.Extensions.fetchFavicon
import ru.tech.easysearch.extensions.Extensions.generateBrowserPopupMenu
import ru.tech.easysearch.extensions.Extensions.generateSideMenu
import ru.tech.easysearch.extensions.Extensions.hideKeyboard
import ru.tech.easysearch.extensions.Extensions.makeScreenshot
import ru.tech.easysearch.extensions.Extensions.setCoeff
import ru.tech.easysearch.extensions.Extensions.shareWith
import ru.tech.easysearch.fragment.bookmarks.BookmarksFragment
import ru.tech.easysearch.fragment.dialog.BookmarkCreationDialog
import ru.tech.easysearch.fragment.dialog.ShortcutCreationDialog
import ru.tech.easysearch.fragment.history.HistoryFragment
import ru.tech.easysearch.fragment.settings.SettingsFragment
import ru.tech.easysearch.fragment.tabs.TabsFragment
import ru.tech.easysearch.functions.Functions
import ru.tech.easysearch.functions.Functions.doInIoThreadWithObservingOnMain
import ru.tech.easysearch.helper.adblock.AdBlocker
import ru.tech.easysearch.helper.adblock.AdBlocker.getDomain
import ru.tech.easysearch.helper.client.ChromeClient
import ru.tech.easysearch.helper.client.WebClient
import ru.tech.easysearch.helper.interfaces.DesktopInterface
import ru.tech.easysearch.helper.utils.anim.AnimUtils.assignAnimations
import ru.tech.easysearch.helper.utils.anim.AnimUtils.fadeIn
import ru.tech.easysearch.helper.utils.anim.AnimUtils.fadeOut
import ru.tech.easysearch.helper.utils.anim.AnimUtils.getAnimInstance
import ru.tech.easysearch.helper.utils.anim.AnimUtils.setAnimListener
import ru.tech.easysearch.helper.utils.save.SaveUtils.addToHomeScreen
import ru.tech.easysearch.helper.utils.save.SaveUtils.saveAsPDF


class BrowserActivity : AppCompatActivity(), DesktopInterface {

    var tempFileCallback: ValueCallback<Array<Uri>>? = null
    val fileChooserResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                tempFileCallback?.onReceiveValue(arrayOf(Uri.parse(data?.dataString)))
            }
        }

    var searchView: TextInputEditText? = null
    var progressBar: LinearProgressIndicator? = null
    var iconView: ImageView? = null

    var backwardBrowser: ImageButton? = null
    var forwardBrowser: ImageButton? = null
    var reloadButton: ImageSwitcher? = null
    var cancelReload: ImageView? = null
    private var startReload: ImageView? = null
    private var goMoreButton: ImageSwitcher? = null
    var webViewContainer: FrameLayout? = null
    private var homeBrowser: ImageButton? = null
    private var currentWindows: ImageButton? = null
    private var profileBrowser: ImageButton? = null
    var bottomAppBar: AppBarLayout? = null
    var root: View? = null

    var lastUrl = ""
    var clickedGo = false

    override fun onStart() {
        super.onStart()
        Functions.doInBackground {
            AdBlocker.createAdList(this)
        }
        assignAnimations()
        if (openedTabs.isEmpty()) loadOpenedTabs(progressBar)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(loadTheme((this)))

        database = ESearchDatabase.getInstance(this)
        setCoeff()

        if (getSetting(this, HIDE_PANELS)) setContentView(R.layout.activity_browser_hide_panels)
        else setContentView(R.layout.activity_browser)

        overridePendingTransition(R.anim.enter_slide_up, R.anim.exit_slide_down)

        searchView = findViewById(R.id.searchView)
        bottomAppBar = findViewById(R.id.bottomAppBar)
        webViewContainer = findViewById(R.id.webViewContainer)
        goMoreButton = findViewById(R.id.goMoreButton)
        progressBar = findViewById(R.id.progressIndicator)
        iconView = findViewById(R.id.icon)
        homeBrowser = findViewById(R.id.homeBrowser)
        backwardBrowser = findViewById(R.id.backwardBrowser)
        forwardBrowser = findViewById(R.id.forwardBrowser)
        profileBrowser = findViewById(R.id.profileBrowser)
        currentWindows = findViewById(R.id.windowsBrowser)
        root = findViewById(R.id.main_root)
        browser = findViewById(R.id.webBrowser)
        reloadButton = findViewById(R.id.reload)
        startReload = findViewById(R.id.start)
        cancelReload = findViewById(R.id.cancel)

        val chromeClient = ChromeClient(this, progressBar!!, browser!!)
        browser!!.webViewClient = WebClient(this, progressBar!!)
        browser!!.webChromeClient = chromeClient

        val url = dispatchIntent(intent)
        if (intent.getBooleanExtra("loadTab", false)) {
            loadTab(intent.getIntExtra("position", -1), false)
        } else {
            searchView?.let { _ ->
                clickedGo = true
                val prefix = "https://www.google.com/search?q="
                val tempUrl = when {
                    !url.contains("https://") && !url.contains("http://") -> "https://$url"
                    else -> url
                }
                if (URLUtil.isValidUrl(tempUrl) && Patterns.WEB_URL.matcher(tempUrl).matches()) {
                    createNewTab(tempUrl)
                    lastUrl = tempUrl
                } else {
                    lastUrl = prefix + url
                    createNewTab(lastUrl)
                }
                searchView!!.setText(lastUrl.getDomain())

                doInIoThreadWithObservingOnMain({
                    fetchFavicon(lastUrl)
                }, {
                    iconView?.setImageBitmap(it as Bitmap)
                })

                browser?.hideKeyboard(this)
                searchView!!.clearFocus()
            }
        }

        if (fadeIn == null) assignAnimations()

        goMoreButton?.inAnimation = fadeIn
        goMoreButton?.outAnimation = fadeOut

        reloadButton?.inAnimation = fadeIn
        reloadButton?.outAnimation = fadeOut

        searchView?.setOnFocusChangeListener { _, focused ->
            if (lastUrl == "") lastUrl = browser?.url!!

            val observableAnimFadeIn = getAnimInstance(true)
            val observableAnimFadeOut = getAnimInstance(false)

            observableAnimFadeIn.setAnimListener(endAction = { reloadButton?.visibility = VISIBLE })

            observableAnimFadeOut.setAnimListener(endAction = { reloadButton?.visibility = GONE })

            when (focused) {
                true -> {
                    reloadButton?.startAnimation(observableAnimFadeOut)
                    goMoreButton?.showNext()
                    goMoreButton?.setOnClickListener {
                        onGetUri(searchView!!, searchView!!.text.toString())
                    }
                    if (!clickedGo) searchView?.setText(lastUrl)
                    searchView?.selectAll()
                }
                false -> {
                    reloadButton?.startAnimation(observableAnimFadeIn)
                    goMoreButton?.showPrevious()
                    goMoreButton?.setOnClickListener { showMore() }
                    if (!clickedGo) searchView?.setText(lastUrl.getDomain())
                }
            }
        }

        searchView?.setOnKeyListener { _, _, keyEvent ->
            var handled = false
            if (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_DOWN) {
                onGetUri(searchView!!, searchView!!.text.toString(), true)
                handled = true
            }
            handled
        }

        homeBrowser?.setOnClickListener {
            saveLastTab()
            webViewContainer?.removeAllViews()
            finish()
            startActivity(Intent(this, MainActivity::class.java))
        }

        currentWindows?.setOnClickListener {
            TabsFragment().show(supportFragmentManager, "windowsOp")
        }

        goMoreButton?.setOnClickListener { showMore() }

        profileBrowser?.setOnClickListener {
            sideMenu = generateSideMenu(root!!.parent as ViewGroup)
                .setMenuItemClickListener { menuItem ->
                    when (menuItem.id) {
                        R.drawable.ic_baseline_history_24 -> {
                            HistoryFragment(browser).show(supportFragmentManager, "custom")
                        }
                        R.drawable.ic_baseline_bookmarks_24 -> {
                            BookmarksFragment(browser).show(supportFragmentManager, "custom")
                        }
                        R.drawable.ic_baseline_download_24 -> {
                            startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))
                        }
                        R.drawable.ic_baseline_settings_24 -> {
                            SettingsFragment().show(supportFragmentManager, "settings")
                        }
                    }
                    sideMenu?.dismiss()
                }
            sideMenu!!.show()
        }

        reloadButton?.setOnClickListener {
            when (reloadButton?.currentView) {
                startReload -> {
                    browser?.reload()
                    reloadButton?.showNext()
                }
                cancelReload -> {
                    browser?.stopLoading()
                    reloadButton?.showPrevious()
                }
            }
        }

        updateGestures()
    }

    private fun showMore() {
        popupMenu = generateBrowserPopupMenu(
            root!!.parent as ViewGroup,
            this
        ).setMenuItemClickListener { popupMenuItem ->
            when (popupMenuItem.id) {
                R.drawable.ic_baseline_refresh_24 -> {
                    browser?.reload()
                }
                R.drawable.ic_baseline_share_24 -> {
                    shareWith(browser?.url.toString())
                }
                R.drawable.ic_baseline_translate_24 -> {
                    browser?.loadUrl("$translateSite${browser?.url}")
                }
                R.drawable.ic_baseline_find_in_page_24 -> {
                    browser?.prepareFinding(root!!.parent as ViewGroup)
                }
                R.drawable.ic_baseline_download_24 -> {
                    browser?.saveAsPDF(this)
                }
                R.drawable.ic_baseline_screenshot_24 -> {
                    webViewContainer?.makeScreenshot(
                        this,
                        root!!.parent as ViewGroup
                    )
                }
                R.drawable.ic_start_panel -> {
                    if (lastUrl == "") lastUrl = browser?.url!!
                    val shortcutDialog = ShortcutCreationDialog(lastUrl, browser?.title!!)
                    if (!shortcutDialog.isAdded) shortcutDialog.show(
                        supportFragmentManager,
                        "shortcutDialog"
                    )
                }
                R.drawable.ic_baseline_bookmark_border_24 -> {
                    if (lastUrl == "") lastUrl = browser?.url!!
                    val bookmarkDialog = BookmarkCreationDialog(lastUrl, browser?.title!!)
                    if (!bookmarkDialog.isAdded) bookmarkDialog.show(
                        supportFragmentManager,
                        "bookDialog"
                    )
                }
                R.drawable.ic_baseline_add_to_home_screen_24 -> {
                    browser?.addToHomeScreen(this)
                }
            }
            popupMenu!!.dismiss()
        }
        popupMenu!!.show()
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun dispatchIntent(intent: Intent): String {
        return when (intent.action) {
            ACTION_VIEW -> intent.dataString.toString()
            ACTION_PROCESS_TEXT -> intent.getCharSequenceExtra(EXTRA_PROCESS_TEXT)
                .toString()
            ACTION_WEB_SEARCH -> intent.getStringExtra(SearchManager.QUERY).toString()
            ACTION_SEND -> intent.getStringExtra(EXTRA_TEXT).toString()
            else -> intent.extras?.get("url").toString()
        }
    }

    var browser: BrowserView? = null

    override fun onBackPressed() {
        when {
            browser?.canGoBack() == true
                    && (sideMenu?.isHidden == true || sideMenu == null)
                    && (popupMenu?.isHidden == true || popupMenu == null)
                    && (browser?.isFinding == false) -> {
                browser?.goBack()
            }
            sideMenu?.isHidden == false -> {
                sideMenu?.dismiss()
            }
            popupMenu?.isHidden == false -> {
                popupMenu?.dismiss()
            }
            browser?.isFinding == true -> {
                browser?.cancelFinding()
            }
            else -> {
                openedTabs.removeLastOrNull()
                updateTabs()
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

    var sideMenu: SideMenu? = null
    var popupMenu: SmartPopupMenu? = null

    @SuppressLint("NotifyDataSetChanged")
    override fun onConfigurationChanged(newConfig: Configuration) {
        sideMenu?.dismiss()
        popupMenu?.dismiss()
        super.onConfigurationChanged(newConfig)
        for (i in supportFragmentManager.fragments) {
            when (i.tag) {
                "windowsOp" -> supportFragmentManager.beginTransaction().remove(i).commit()
                "settings" -> (i as SettingsFragment).adapter?.notifyDataSetChanged()
            }
        }
    }

    override fun changeUserAgent(isChecked: Boolean) {
        browser?.settings!!.apply {
            userAgentString = when (isChecked) {
                true -> DataArrays.desktopUserAgentString
                else -> DataArrays.userAgentString
            }
            useWideViewPort = isChecked
            loadWithOverviewMode = isChecked
        }
        browser?.reload()
    }

    override fun onStop() {
        updateTabs()
        super.onStop()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent?.let { createNewTab(dispatchIntent(it)) }
        for (i in supportFragmentManager.fragments) supportFragmentManager.beginTransaction()
            .remove(i).commit()
    }

}