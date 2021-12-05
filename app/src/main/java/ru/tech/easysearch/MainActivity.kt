package ru.tech.easysearch

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.webkit.WebView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bekawestberg.loopinglayout.library.LoopingLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.tech.easysearch.R.drawable.*
import ru.tech.easysearch.adapter.ToolbarAdapter
import java.util.*


class MainActivity : AppCompatActivity() {

    private var searchView: SearchView? = null
    private var adapter: ToolbarAdapter? = null

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                recognized(data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS))
            }
        }

    private fun recognized(stringArrayListExtra: ArrayList<String>?) {
        var uriString = ""
        if (stringArrayListExtra != null) {
            for (i in stringArrayListExtra) {
                uriString += "$i+"
            }
        }
        val result: String = stringArrayListExtra.toString().drop(1).dropLast(1)
        searchView?.findViewById<AppCompatImageView>(androidx.appcompat.R.id.search_button)
            ?.performClick()
        searchView?.setQuery(result, true)
        searchView?.clearFocus()

        uriString.dropLast(1)
        onGetUri(uriString)

    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_ESearch)
        setContentView(R.layout.activity_main)

        browser = findViewById(R.id.webBrowser)
        browser!!.webViewClient = WebClient()
        browser!!.settings.javaScriptEnabled = true
        browser!!.loadUrl("https://www.google.com")

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
                ic_youtube_logo
            )
        val layoutManager = LoopingLayoutManager(
            this,
            LoopingLayoutManager.HORIZONTAL,
            false
        )
        recycler.layoutManager = layoutManager

        adapter = ToolbarAdapter(this, labelList)
        recycler.adapter = adapter

        searchView = findViewById(R.id.searchView)

        val helper = PagerSnapHelper()
        helper.attachToRecyclerView(recycler)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            startSpeechRecognize(resultLauncher)
        }

        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                when (adapter?.labelList?.get((recyclerView.layoutManager as LoopingLayoutManager).findLastCompletelyVisibleItemPosition())) {
                    ic_google_logo -> prefix = "https://www.google.com/search?q="
                    ic_yandex_logo -> prefix = "https://yandex.ru/search/touch/?text="
                    ic_bing_logo -> prefix = "https://www.bing.com/search?q="
                }
            }
        })
    }

    private fun startSpeechRecognize(resultLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        resultLauncher.launch(intent)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        var browser: WebView? = null
    }

    override fun onBackPressed() {
        if (browser?.canGoBack() == true) browser?.goBack()
        else super.onBackPressed()
    }

    private var prefix = ""

    private fun onGetUri(uriLast: String) {
        browser?.loadUrl(prefix + uriLast)
    }

}