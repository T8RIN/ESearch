package ru.tech.easysearch.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Patterns
import android.view.View.GONE
import android.webkit.URLUtil
import android.webkit.WebView
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bekawestberg.loopinglayout.library.LoopingLayoutManager
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.*
import ru.tech.easysearch.R
import ru.tech.easysearch.activity.MainActivity.Companion.displayOffsetX
import ru.tech.easysearch.activity.MainActivity.Companion.displayOffsetY
import ru.tech.easysearch.adapter.toolbar.ToolbarAdapter
import ru.tech.easysearch.data.DataArrays
import ru.tech.easysearch.data.SharedPreferencesAccess.loadLabelList
import ru.tech.easysearch.fragment.dialog.SelectLabels
import ru.tech.easysearch.helper.client.ChromeClient
import ru.tech.easysearch.helper.client.WebClient
import ru.tech.easysearch.helper.interfaces.LabelListChangedInterface

class SearchResultsActivity : AppCompatActivity(), LabelListChangedInterface {

    private var searchView: SearchView? = null
    private var toolbarAdapter: ToolbarAdapter? = null
    private var layoutManager: LoopingLayoutManager? = null
    private var card: MaterialCardView? = null
    private var labelRecycler: RecyclerView? = null
    private var fab: FloatingActionButton? = null
    private var manageList: ImageButton? = null
    private var close: ImageButton? = null
    private var backButton: ImageButton? = null
    private var progressBar: LinearProgressIndicator? = null

    override fun onStart() {
        super.onStart()
        displayOffsetY = -resources.displayMetrics.heightPixels.toFloat()
        displayOffsetX = -resources.displayMetrics.widthPixels.toFloat()
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_results)

        overridePendingTransition(R.anim.enter_slide_up, R.anim.exit_slide_down)

        searchView = findViewById(R.id.searchView)
        progressBar = findViewById(R.id.progressIndicator)
        val recycler: RecyclerView = findViewById(R.id.toolbarRecycler)

        browser = findViewById(R.id.webBrowser)
        browser!!.webViewClient = WebClient(this, recycler, progressBar!!)
        val settings = browser!!.settings
        settings.javaScriptEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.supportMultipleWindows()
        settings.userAgentString = DataArrays.userAgentString

        browser!!.webChromeClient = ChromeClient(this, progressBar!!)

        prefix = intent.extras?.get("prefix").toString()
        val query = intent.extras?.get("url").toString().removePrefix(prefix)
        onGetUri(query)

        if (savedInstanceState != null) browser!!.restoreState(savedInstanceState.getBundle("webViewState")!!)

        displayOffsetY = -resources.displayMetrics.heightPixels.toFloat()
        displayOffsetX = -resources.displayMetrics.widthPixels.toFloat()


        backButton = findViewById(R.id.backButton)

        val labelList: ArrayList<Int> = ArrayList()
        for (i in loadLabelList(this)!!.split("+")) labelList.add(i.toInt())

        layoutManager = LoopingLayoutManager(
            this,
            LoopingLayoutManager.HORIZONTAL,
            false
        )
        recycler.layoutManager = layoutManager

        card = findViewById(R.id.labelSuggestionCard)
        card!!.translationY = displayOffsetY
        manageList = findViewById(R.id.manageList)
        manageList!!.translationY = displayOffsetY
        close = findViewById(R.id.closeButton)
        close!!.translationX = displayOffsetX

        val labelRecycler: RecyclerView = findViewById(R.id.labelRecycler)

        toolbarAdapter = ToolbarAdapter(
            this,
            labelList,
            card!!,
            null,
            labelRecycler,
            recycler,
            null,
            null,
            manageList!!,
            close!!
        )
        recycler.adapter = toolbarAdapter

        layoutManager!!.scrollToPosition(
            toolbarAdapter!!.labelList.indexOf(
                DataArrays.prefixDict.filterValues { it == prefix }.keys.elementAt(0)
            )
        )

        val selectLabelsFragment = SelectLabels(this)
        manageList!!.setOnClickListener {
            if (!selectLabelsFragment.isAdded) selectLabelsFragment.show(
                supportFragmentManager,
                "custom"
            )
        }

        searchView!!.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String): Boolean {
                    if (newText.isNotEmpty()) uriLast = newText
                    return true
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    onGetUri(query)
                    searchView!!.clearFocus()
                    return true
                }
            })

        searchView?.findViewById<AppCompatImageView>(androidx.appcompat.R.id.search_button)
            ?.performClick()
        searchView?.setQuery(query, false)
        searchView?.clearFocus()


        val helper = PagerSnapHelper()
        helper.attachToRecyclerView(recycler)

        var animating = false

        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!animating && card!!.translationY == 0f) {
                    card!!.animate().y(displayOffsetY).setDuration(300)
                        .withStartAction { animating = true }
                        .withEndAction { animating = false }.start()
                    close!!.animate().x(displayOffsetX).setDuration(200).start()
                    manageList!!.animate().y(displayOffsetY).setDuration(200).start()
                    backButton!!.animate().y(0f).setDuration(200).start()
                }
                job?.cancel()
                job = CoroutineScope(Dispatchers.Main).launch {
                    waitFor(300)
                    val key =
                        layoutManager?.findLastCompletelyVisibleItemPosition()
                            ?.let { toolbarAdapter?.labelList?.get(it) }
                    prefix = DataArrays.prefixDict[key]!!
                    if (uriLast.isNotEmpty()) onGetUri(uriLast)
                }
            }
        })

        close!!.setOnClickListener {
            card!!.animate().y(displayOffsetY).setStartDelay(100).setDuration(300).start()
            close!!.animate().x(displayOffsetX).setDuration(200).start()
            manageList!!.animate().y(displayOffsetY).setDuration(200).start()
            backButton!!.animate().y(0f).setDuration(200).start()
        }

        backButton!!.setOnClickListener { onBackPressed() }

    }

    private var job: Job? = null

    private suspend fun waitFor(time: Long) = withContext(Dispatchers.IO) { delay(time) }

    private var browser: WebView? = null

    override fun onBackPressed() {
        when {
            browser?.canGoBack() == true -> browser?.goBack()
            card?.translationY == 0f -> {
                card?.animate()?.y(displayOffsetY)?.setDuration(350)?.withEndAction {
                    labelRecycler?.adapter = null
                    fab?.show()
                    card?.visibility = GONE
                }?.start()
                close?.animate()?.x(displayOffsetX)?.setDuration(200)?.start()
                manageList?.animate()?.y(displayOffsetY)?.setDuration(200)?.start()
                backButton?.animate()?.y(0f)?.setDuration(200)?.start()
            }
            else -> {
                super.onBackPressed()
                overridePendingTransition(R.anim.enter_slide_up, R.anim.exit_slide_down)
            }
        }
    }

    private var prefix = ""

    private fun onGetUri(uriLast: String) {
        this.uriLast = uriLast
        val tempUrl = when {
            !uriLast.contains("https://") && !uriLast.contains("http://") -> "https://$uriLast"
            else -> uriLast
        }
        if (URLUtil.isValidUrl(tempUrl) && Patterns.WEB_URL.matcher(tempUrl).matches()) {
            browser?.loadUrl(tempUrl)
        } else {
            browser?.loadUrl(prefix + uriLast)
        }

    }

    private var uriLast: String = ""

    override fun onSaveInstanceState(outState: Bundle) {
        val bundle = Bundle()
        browser?.saveState(bundle)
        outState.putBundle("webViewState", bundle)
        super.onSaveInstanceState(outState)
    }

    override fun onEndList() {}

    @SuppressLint("NotifyDataSetChanged")
    override fun onStartList(labelList: ArrayList<Int>) {
        toolbarAdapter?.labelList = labelList
        toolbarAdapter?.notifyDataSetChanged()
        toolbarAdapter?.labelListAdapter?.labelList = labelList
        toolbarAdapter?.labelListAdapter?.notifyDataSetChanged()
    }

}