package ru.tech.easysearch.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import android.widget.ImageButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bekawestberg.loopinglayout.library.LoopingLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.tech.easysearch.R
import ru.tech.easysearch.data.DataArrays.prefixDict
import ru.tech.easysearch.R.drawable.*
import ru.tech.easysearch.adapter.ToolbarAdapter
import ru.tech.easysearch.fragment.history.HistoryFragment
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
        startBrowserWithUri(uriString)
    }

    private fun startBrowserWithUri(uriString: String) {
        val intent = Intent(this, BrowserActivity::class.java)
        intent.putExtra("url", prefix + uriString)
        intent.putExtra("prefix", prefix)
        startActivity(intent)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_ESearch)
        setContentView(R.layout.activity_main)

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

        searchView = findViewById(R.id.searchView)
        searchView!!.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String): Boolean {

                    return true
                }

                override fun onQueryTextSubmit(query: String): Boolean {

                    return true
                }
            })


        val helper = PagerSnapHelper()
        helper.attachToRecyclerView(recycler)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            startSpeechRecognize(resultLauncher)
        }
        val forward: ImageButton = findViewById(R.id.forward)
        val backward: ImageButton = findViewById(R.id.backward)

        recursiveClickForward(forward, layoutManager, recycler)
        recursiveClickBackward(backward, layoutManager, recycler)

        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val key =
                    adapter?.labelList?.get((recyclerView.layoutManager as LoopingLayoutManager).findLastCompletelyVisibleItemPosition())
                prefix = prefixDict[key]!!

            }
        })
        
        val history: ImageButton = findViewById(R.id.historyButton)
        history.setOnClickListener { HistoryFragment().show(supportFragmentManager, "custom")}
        
    }

    private var prefix = ""

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

    private fun startSpeechRecognize(resultLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        resultLauncher.launch(intent)
    }

}