package ru.tech.easysearch.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.webkit.WebView
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bekawestberg.loopinglayout.library.LoopingLayoutManager
import ru.tech.easysearch.R
import ru.tech.easysearch.R.drawable.*
import ru.tech.easysearch.adapter.ToolbarAdapter
import ru.tech.easysearch.data.DataArrays
import ru.tech.easysearch.helper.client.WebClient

class BrowserActivity : AppCompatActivity() {

    private var searchView: SearchView? = null
    private var adapter: ToolbarAdapter? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser)

        overridePendingTransition(R.anim.enter_slide_up, R.anim.exit_slide_down)

        browser = findViewById(R.id.webBrowser)
        browser!!.webViewClient = WebClient()
        browser!!.settings.javaScriptEnabled = true
        browser!!.settings.builtInZoomControls = true

        if (savedInstanceState != null) browser!!.restoreState(savedInstanceState.getBundle("webViewState")!!)

        prefix = intent.extras?.get("prefix").toString()

        onGetUri(intent.extras?.get("url").toString().removePrefix(prefix))

        val recycler: RecyclerView = findViewById(R.id.toolbarRecycler)
        val labelList =
            listOf(
                ic_google_logo,
                ic_bing_logo,
                ic_yandex_logo,
                ic_amazon_logo,
                ic_avito_logo,
                ic_yahoo_logo,
                ic_translate_logo,
                ic_duckduckgo_logo,
                ic_ebay_logo,
                ic_ekatalog_logo,
                ic_facebook_logo,
                ic_imdb_logo,
                ic_mailru_logo,
                ic_ozon_logo,
                ic_twitter_logo,
                ic_vk_logo,
                ic_wikipedia_logo,
                ic_youla_logo,
                ic_youtube_logo,
                ic_github_logo
            )
        val layoutManager = LoopingLayoutManager(
            this,
            LoopingLayoutManager.HORIZONTAL,
            false
        )
        recycler.layoutManager = layoutManager

        adapter = ToolbarAdapter(this, labelList)
        recycler.adapter = adapter

        layoutManager.scrollToPosition(
            adapter!!.labelList.indexOf(
                DataArrays.prefixDict.filterValues { it == prefix }.keys.elementAt(
                    0
                )
            )
        )

        searchView = findViewById(R.id.searchView)
        searchView!!.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String): Boolean {
                    if (newText.isNotEmpty()) uriLast = newText
                    return true
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    onGetUri(query)
                    return true
                }
            })


        val helper = PagerSnapHelper()
        helper.attachToRecyclerView(recycler)

        val forward: ImageButton = findViewById(R.id.forward)
        val backward: ImageButton = findViewById(R.id.backward)

        recursiveClickForward(forward, layoutManager, recycler)
        recursiveClickBackward(backward, layoutManager, recycler)

        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val key =
                    adapter?.labelList?.get((recyclerView.layoutManager as LoopingLayoutManager).findLastCompletelyVisibleItemPosition())
                prefix = DataArrays.prefixDict[key]!!
                if (uriLast.isNotEmpty()) onGetUri(uriLast)
            }
        })
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        var browser: WebView? = null
    }

    override fun onBackPressed() {
        if (browser?.canGoBack() == true) browser?.goBack()
        else {
            super.onBackPressed()
            overridePendingTransition(R.anim.enter_slide_up, R.anim.exit_slide_down)
        }
    }

    private var prefix = ""

    private fun onGetUri(uriLast: String) {
        this.uriLast = uriLast
        browser?.loadUrl(prefix + uriLast)
    }

    private var uriLast: String = ""

    override fun onSaveInstanceState(outState: Bundle) {
        val bundle = Bundle()
        browser?.saveState(bundle)
        outState.putBundle("webViewState", bundle)
        super.onSaveInstanceState(outState)
    }

    private fun recursiveClickForward(
        forward: ImageButton,
        layoutManager: LoopingLayoutManager,
        recycler: RecyclerView
    ) {
        forward.setOnClickListener {
            val newPos = layoutManager.findLastCompletelyVisibleItemPosition() + 1
            if (newPos == adapter!!.itemCount) recycler.scrollToPosition(0)
            else recycler.smoothScrollToPosition(newPos)
            forward.setOnClickListener {}
            Handler(mainLooper).postDelayed({
                recursiveClickForward(
                    forward,
                    layoutManager,
                    recycler
                )
            }, 200)
        }
    }

    private fun recursiveClickBackward(
        backward: ImageButton,
        layoutManager: LoopingLayoutManager,
        recycler: RecyclerView
    ) {
        backward.setOnClickListener {
            val newPos = layoutManager.findLastCompletelyVisibleItemPosition() - 1
            if (newPos == -1) recycler.scrollToPosition(adapter!!.itemCount)
            else recycler.smoothScrollToPosition(newPos)
            backward.setOnClickListener {}
            Handler(mainLooper).postDelayed({
                recursiveClickBackward(
                    backward,
                    layoutManager,
                    recycler
                )
            }, 200)
        }
    }


}