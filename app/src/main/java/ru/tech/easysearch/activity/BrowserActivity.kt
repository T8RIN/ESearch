package ru.tech.easysearch.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
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
import ru.tech.easysearch.R
import ru.tech.easysearch.activity.MainActivity.Companion.displayOffsetX
import ru.tech.easysearch.activity.MainActivity.Companion.displayOffsetY
import ru.tech.easysearch.adapter.toolbar.ToolbarAdapter
import ru.tech.easysearch.data.DataArrays
import ru.tech.easysearch.data.SharedPreferencesAccess.loadLabelList
import ru.tech.easysearch.fragment.dialog.SelectLabels
import ru.tech.easysearch.helper.client.WebClient
import ru.tech.easysearch.helper.interfaces.LabelListChangedInterface

class BrowserActivity : AppCompatActivity(), LabelListChangedInterface {

    private var searchView: SearchView? = null
    private var toolbarAdapter: ToolbarAdapter? = null
    private var layoutManager: LoopingLayoutManager? = null

    override fun onStart() {
        super.onStart()
        displayOffsetY = -resources.displayMetrics.heightPixels.toFloat()
        displayOffsetX = -resources.displayMetrics.widthPixels.toFloat()
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser)

        overridePendingTransition(R.anim.enter_slide_up, R.anim.exit_slide_down)

        browser = findViewById(R.id.webBrowser)
        browser!!.webViewClient = WebClient()
        val settings = browser!!.settings
        settings.javaScriptEnabled = true
        settings.builtInZoomControls = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.supportMultipleWindows()

        if (savedInstanceState != null) browser!!.restoreState(savedInstanceState.getBundle("webViewState")!!)

        displayOffsetY = -resources.displayMetrics.heightPixels.toFloat()
        displayOffsetX = -resources.displayMetrics.widthPixels.toFloat()

        prefix = intent.extras?.get("prefix").toString()

        val backButton: ImageButton = findViewById(R.id.backButton)

        val query = intent.extras?.get("url").toString().removePrefix(prefix)

        onGetUri(query)

        val recycler: RecyclerView = findViewById(R.id.toolbarRecycler)

        val labelList: ArrayList<Int> = ArrayList()
        for (i in loadLabelList(this)!!.split("+")) labelList.add(i.toInt())

        layoutManager = LoopingLayoutManager(
            this,
            LoopingLayoutManager.HORIZONTAL,
            false
        )
        recycler.layoutManager = layoutManager

        val card: MaterialCardView = findViewById(R.id.labelSuggestionCard)
        card.translationY = displayOffsetY
        val manageList: ImageButton = findViewById(R.id.manageList)
        manageList.translationY = displayOffsetY
        val close: ImageButton = findViewById(R.id.closeButton)
        close.translationX = displayOffsetX

        val labelRecycler: RecyclerView = findViewById(R.id.labelRecycler)

        toolbarAdapter = ToolbarAdapter(
            this,
            labelList,
            card,
            null,
            labelRecycler,
            recycler,
            null,
            null,
            manageList,
            close
        )
        recycler.adapter = toolbarAdapter

        layoutManager!!.scrollToPosition(
            toolbarAdapter!!.labelList.indexOf(
                DataArrays.prefixDict.filterValues { it == prefix }.keys.elementAt(0)
            )
        )

        val selectLabelsFragment = SelectLabels(this)
        manageList.setOnClickListener {
            if (!selectLabelsFragment.isAdded) selectLabelsFragment.show(
                supportFragmentManager,
                "custom"
            )
        }

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
                val key =
                    layoutManager?.findLastCompletelyVisibleItemPosition()
                        ?.let { toolbarAdapter?.labelList?.get(it) }
                prefix = DataArrays.prefixDict[key]!!
                if (uriLast.isNotEmpty()) onGetUri(uriLast)
                if (!animating && card.translationY == 0f) {
                    card.animate().y(displayOffsetY).setDuration(300)
                        .withStartAction { animating = true }
                        .withEndAction { animating = false }.start()
                    close.animate().x(displayOffsetX).setDuration(200).start()
                    manageList.animate().y(displayOffsetY).setDuration(200).start()
                    backButton.animate().y(0f).setDuration(200).start()
                }
            }
        })

        close.setOnClickListener {
            card.animate().y(displayOffsetY).setStartDelay(100).setDuration(300).start()
            close.animate().x(displayOffsetX).setDuration(200).start()
            manageList.animate().y(displayOffsetY).setDuration(200).start()
            backButton.animate().y(0f).setDuration(200).start()
        }

        backButton.setOnClickListener { onBackPressed() }

    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        var browser: WebView? = null
    }

    override fun onBackPressed() {
        val card = findViewById<MaterialCardView>(R.id.labelSuggestionCard)
        val labelRecycler = findViewById<RecyclerView>(R.id.labelRecycler)
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        val manageList = findViewById<ImageButton>(R.id.manageList)
        val close = findViewById<ImageButton>(R.id.closeButton)
        val backButton = findViewById<ImageButton>(R.id.backButton)
        when {
            browser?.canGoBack() == true -> browser?.goBack()
            card.translationY == 0f -> {
                card.animate()
                    .y(displayOffsetY)
                    .setDuration(350)
                    .withEndAction {
                        labelRecycler.adapter = null
                        fab?.show()
                        card.visibility = View.GONE
                    }
                    .start()
                close.animate().x(displayOffsetX).setDuration(200).start()
                manageList.animate().y(displayOffsetY).setDuration(200).start()
                backButton.animate().y(0f).setDuration(200).start()
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
        browser?.loadUrl(prefix + uriLast)
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