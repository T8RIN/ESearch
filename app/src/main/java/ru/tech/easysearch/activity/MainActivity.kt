package ru.tech.easysearch.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import android.view.View
import android.widget.ImageButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bekawestberg.loopinglayout.library.LoopingLayoutManager
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.tech.easysearch.R
import ru.tech.easysearch.adapter.toolbar.ToolbarAdapter
import ru.tech.easysearch.data.DataArrays.prefixDict
import ru.tech.easysearch.data.SharedPreferencesAccess.loadLabelList
import ru.tech.easysearch.fragment.dialog.SelectLabels
import ru.tech.easysearch.fragment.recent.RecentFragment
import ru.tech.easysearch.fragment.settings.SettingsFragment
import ru.tech.easysearch.fragment.vpn.VpnFragment
import ru.tech.easysearch.helper.interfaces.LabelListChangedInterface
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), LabelListChangedInterface {

    private var searchView: SearchView? = null
    private var toolbarAdapter: ToolbarAdapter? = null
    private var layoutManager: LoopingLayoutManager? = null

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                recognized(data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS))
            }
        }

    private fun recognized(stringArrayListExtra: ArrayList<String>?) {

        val result: String = stringArrayListExtra.toString().drop(1).dropLast(1)
        searchView?.findViewById<AppCompatImageView>(androidx.appcompat.R.id.search_button)
            ?.performClick()
        searchView?.setQuery(result, true)
        searchView?.clearFocus()

        startBrowserWithQuery(result)
    }

    private fun startBrowserWithQuery(query: String) {
        val intent = Intent(this, BrowserActivity::class.java)
        val key =
            layoutManager?.findLastCompletelyVisibleItemPosition()
                ?.let { toolbarAdapter?.labelList?.get(it) }
        val prefix = prefixDict[key]!!
        intent.putExtra("url", prefix + query)
        intent.putExtra("prefix", prefix)
        startActivity(intent)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_ESearch)

        super.onCreate(savedInstanceState)

        overridePendingTransition(R.anim.enter_slide_up, R.anim.exit_slide_down)

        setContentView(R.layout.activity_main)

        displayOffsetY = -resources.displayMetrics.heightPixels.toFloat()
        displayOffsetX = -resources.displayMetrics.widthPixels.toFloat()

        val recycler: RecyclerView = findViewById(R.id.toolbarRecycler)

        val labelList: ArrayList<Int> = ArrayList()
        for (i in loadLabelList(this)!!.split("+")) labelList.add(i.toInt())

        layoutManager = LoopingLayoutManager(
            this,
            LoopingLayoutManager.HORIZONTAL,
            false
        )
        recycler.layoutManager = layoutManager

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            startSpeechRecognize(resultLauncher)
        }

        val card: MaterialCardView = findViewById(R.id.labelSuggestionCard)
        card.translationY = displayOffsetY
        val manageList: ImageButton = findViewById(R.id.manageList)
        manageList.translationY = displayOffsetY
        val close: ImageButton = findViewById(R.id.closeButton)
        close.translationX = displayOffsetX

        val labelRecycler: RecyclerView = findViewById(R.id.labelRecycler)

        val forward: ImageButton = findViewById(R.id.forward)
        val backward: ImageButton = findViewById(R.id.backward)

        recursiveClickForward(forward, layoutManager!!, recycler)
        recursiveClickBackward(backward, layoutManager!!, recycler)

        toolbarAdapter =
            ToolbarAdapter(
                this,
                labelList,
                card,
                fab,
                labelRecycler,
                recycler,
                forward,
                backward,
                manageList,
                close
            )
        recycler.adapter = toolbarAdapter

        val selectLabelsFragment = SelectLabels(this)
        manageList.setOnClickListener {
            if (!selectLabelsFragment.isAdded) selectLabelsFragment.show(
                supportFragmentManager,
                "custom"
            )
        }

        close.setOnClickListener {
            card.animate().y(displayOffsetY).setStartDelay(100).setDuration(300)
                .withEndAction { fab.show() }.start()
            forward.animate().y(0f).setDuration(300).start()
            backward.animate().y(0f).setDuration(300).start()
            close.animate().x(displayOffsetX).setDuration(200).start()
            manageList.animate().y(displayOffsetY).setDuration(200).start()
        }

        searchView = findViewById(R.id.searchView)
        searchView!!.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String): Boolean {

                    return true
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    startBrowserWithQuery(query)
                    return true
                }
            })

        searchView?.findViewById<AppCompatImageView>(androidx.appcompat.R.id.search_button)
            ?.performClick()
        searchView?.clearFocus()


        val helper = PagerSnapHelper()
        helper.attachToRecyclerView(recycler)

        var animating = false

        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(!animating && card.translationY == 0f){
                    card.animate().y(displayOffsetY).setDuration(300)
                        .withStartAction { animating = true }
                        .withEndAction {
                            fab.show()
                            animating = false
                        }.start()
                    forward.animate().y(0f).setDuration(300).start()
                    backward.animate().y(0f).setDuration(300).start()
                    close.animate().x(displayOffsetX).setDuration(200).start()
                    manageList.animate().y(displayOffsetY).setDuration(200).start()
                }
            }
        })

        val history: ImageButton = findViewById(R.id.historyButton)
        val vpn: ImageButton = findViewById(R.id.vpnButton)
        val search: ImageButton = findViewById(R.id.searchButton)
        val settings: ImageButton = findViewById(R.id.settingsButton)
        recursiveBottomNavigationClick(history, vpn, search, settings)
    }

    private fun recursiveBottomNavigationClick(
        history: ImageButton,
        vpn: ImageButton,
        search: ImageButton,
        settings: ImageButton
    ) {
        history.setOnClickListener {
            RecentFragment().show(supportFragmentManager, "custom")
            delayBeforeNextClick(history, vpn, search, settings)
        }
        vpn.setOnClickListener {
            VpnFragment().show(supportFragmentManager, "custom")
            delayBeforeNextClick(history, vpn, search, settings)
        }
        search.setOnClickListener {
            startBrowserWithQuery(searchView!!.query.toString())
            delayBeforeNextClick(history, vpn, search, settings)
        }
        settings.setOnClickListener {
            SettingsFragment().show(supportFragmentManager, "custom")
            delayBeforeNextClick(history, vpn, search, settings)
        }
    }

    private fun delayBeforeNextClick(
        history: ImageButton,
        vpn: ImageButton,
        search: ImageButton,
        settings: ImageButton
    ) {
        clearListeners(history, vpn, search, settings)
        Handler(mainLooper).postDelayed({
            recursiveBottomNavigationClick(history, vpn, search, settings)
        }, 300)
    }

    private fun clearListeners(
        history: ImageButton,
        vpn: ImageButton,
        search: ImageButton,
        settings: ImageButton
    ) {
        history.setOnClickListener {}
        vpn.setOnClickListener {}
        search.setOnClickListener {}
        settings.setOnClickListener {}
    }

    private fun recursiveClickForward(
        forward: ImageButton,
        layoutManager: LoopingLayoutManager,
        recycler: RecyclerView
    ) {
        forward.setOnClickListener {
            val newPos = layoutManager.findLastCompletelyVisibleItemPosition() + 1
            if (newPos == toolbarAdapter!!.itemCount) recycler.scrollToPosition(0)
            else recycler.smoothScrollToPosition(newPos)
            it.setOnClickListener {}
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
            if (newPos == -1) recycler.scrollToPosition(toolbarAdapter!!.itemCount)
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

    companion object {
        var displayOffsetY = -5000f
        var displayOffsetX = -1000f
    }

    override fun onBackPressed(){
        val card = findViewById<MaterialCardView>(R.id.labelSuggestionCard)
        val labelRecycler = findViewById<RecyclerView>(R.id.labelRecycler)
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        val forward = findViewById<ImageButton>(R.id.forward)
        val backward = findViewById<ImageButton>(R.id.backward)
        val manageList = findViewById<ImageButton>(R.id.manageList)
        val close = findViewById<ImageButton>(R.id.closeButton)
        if(card.translationY == 0f) {
            card.animate()
                .y(displayOffsetY)
                .setDuration(350)
                .withEndAction {
                    labelRecycler.adapter = null
                    fab?.show()
                    card.visibility = View.GONE
                }
                .start()
            forward.animate().y(0f).setDuration(300).start()
            backward.animate().y(0f).setDuration(300).start()
            close.animate().x(displayOffsetX).setDuration(200).start()
            manageList.animate().y(displayOffsetY).setDuration(200).start()
        }
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