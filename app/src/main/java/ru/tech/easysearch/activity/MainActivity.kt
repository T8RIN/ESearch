package ru.tech.easysearch.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import android.util.Patterns
import android.view.View
import android.webkit.URLUtil
import android.widget.ImageButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bekawestberg.loopinglayout.library.LoopingLayoutManager
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.tech.easysearch.R
import ru.tech.easysearch.adapter.shortcuts.ShortcutsPagerRecyclerAdapter
import ru.tech.easysearch.adapter.toolbar.ToolbarAdapter
import ru.tech.easysearch.application.ESearchApplication.Companion.database
import ru.tech.easysearch.data.DataArrays.prefixDict
import ru.tech.easysearch.data.SharedPreferencesAccess.loadLabelList
import ru.tech.easysearch.database.ESearchDatabase
import ru.tech.easysearch.database.shortcuts.Shortcut
import ru.tech.easysearch.databinding.ActivityMainBinding
import ru.tech.easysearch.extensions.Extensions.getBitmap
import ru.tech.easysearch.extensions.Extensions.setCoeff
import ru.tech.easysearch.extensions.Extensions.toByteArray
import ru.tech.easysearch.fragment.bookmarks.BookmarksFragment
import ru.tech.easysearch.fragment.current.CurrentWindowsFragment
import ru.tech.easysearch.fragment.dialog.SelectLabelsDialog
import ru.tech.easysearch.fragment.history.HistoryFragment
import ru.tech.easysearch.fragment.settings.SettingsFragment
import ru.tech.easysearch.helper.interfaces.LabelListChangedInterface
import java.util.*

@SuppressLint("NotifyDataSetChanged")
class MainActivity : AppCompatActivity(), LabelListChangedInterface {

    private lateinit var binding: ActivityMainBinding

    private var searchView: SearchView? = null
    private var toolbarAdapter: ToolbarAdapter? = null
    private var layoutManager: LoopingLayoutManager? = null
    private var card: MaterialCardView? = null
    private var labelRecycler: RecyclerView? = null
    private var toolbarRecycler: RecyclerView? = null
    private var fab: FloatingActionButton? = null
    private var forward: ImageButton? = null
    private var backward: ImageButton? = null
    private var manageList: ImageButton? = null
    private var close: ImageButton? = null

    private var history: ImageButton? = null
    private var currentWindows: ImageButton? = null
    private var bookmarks: ImageButton? = null
    private var settings: ImageButton? = null

    private var pagerShapHelper = PagerSnapHelper()

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
        val intent = Intent(this, SearchResultsActivity::class.java)
        val key =
            layoutManager?.findLastCompletelyVisibleItemPosition()
                ?.let { toolbarAdapter?.labelList?.get(it) }
        val prefix = prefixDict[key]!!

        val tempUrl = when {
            !query.contains("https://") && !query.contains("http://") -> "https://$query"
            else -> query
        }
        if (URLUtil.isValidUrl(tempUrl) && Patterns.WEB_URL.matcher(tempUrl).matches()) {
            val intentBrowser = Intent(this, BrowserActivity::class.java)
            intentBrowser.putExtra("url", tempUrl)
            startActivity(intentBrowser)
        } else {
            intent.putExtra("url", prefix + query)
            intent.putExtra("prefix", prefix)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        val labelList: ArrayList<String> = ArrayList(loadLabelList(this)!!.split("+"))

        toolbarAdapter?.labelList = labelList
        toolbarAdapter?.notifyDataSetChanged()
        toolbarAdapter?.labelListAdapter?.labelList = labelList
        toolbarAdapter?.labelListAdapter?.notifyDataSetChanged()
        displayOffsetY = -resources.displayMetrics.heightPixels.toFloat()
        displayOffsetX = -resources.displayMetrics.widthPixels.toFloat()
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_ESearch)

        super.onCreate(savedInstanceState)

        overridePendingTransition(R.anim.enter_slide_up, R.anim.exit_slide_down)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setCoeff()

        database = ESearchDatabase.getInstance(applicationContext)

        displayOffsetY = -resources.displayMetrics.heightPixels.toFloat()
        displayOffsetX = -resources.displayMetrics.widthPixels.toFloat()

        toolbarRecycler = binding.appBar.toolbarRecycler

        val labelList: ArrayList<String> = ArrayList(loadLabelList(this)!!.split("+"))

        layoutManager = LoopingLayoutManager(
            this,
            LoopingLayoutManager.HORIZONTAL,
            false
        )
        toolbarRecycler!!.layoutManager = layoutManager

        fab = binding.fab
        fab!!.setOnClickListener {
            startSpeechRecognize(resultLauncher)
        }

        card = binding.labelSuggestionCard
        card!!.translationY = displayOffsetY
        manageList = binding.manageList
        manageList!!.translationY = displayOffsetY
        close = binding.closeButton
        close!!.translationX = displayOffsetX

        history = binding.historyButton
        currentWindows = binding.currentPagesButton
        bookmarks = binding.bookmarksButton
        settings = binding.settingsButton

        labelRecycler = binding.labelRecycler

        forward = binding.forward
        backward = binding.backward

        recursiveClickForward()
        recursiveClickBackward()

        toolbarAdapter =
            ToolbarAdapter(
                this,
                labelList,
                card!!,
                fab,
                labelRecycler!!,
                toolbarRecycler!!,
                forward,
                backward,
                manageList!!,
                close!!
            )
        toolbarRecycler!!.adapter = toolbarAdapter

        val selectLabelsFragment = SelectLabelsDialog(this)
        manageList!!.setOnClickListener {
            if (!selectLabelsFragment.isAdded) selectLabelsFragment.show(
                supportFragmentManager,
                "custom"
            )
        }

        close!!.setOnClickListener {
            card!!.animate().y(displayOffsetY).setStartDelay(100).setDuration(300)
                .withEndAction { fab!!.show() }.start()
            forward!!.animate().y(0f).setDuration(300).start()
            backward!!.animate().y(0f).setDuration(300).start()
            close!!.animate().x(displayOffsetX).setDuration(200).start()
            manageList!!.animate().y(displayOffsetY).setDuration(200).start()
        }

        searchView = binding.searchView
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
        helper.attachToRecyclerView(toolbarRecycler!!)

        var animating = false


        toolbarRecycler!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!animating && card!!.translationY == 0f) {
                    card!!.animate().y(displayOffsetY).setDuration(300)
                        .withStartAction { animating = true }
                        .withEndAction {
                            fab!!.show()
                            animating = false
                        }.start()
                    forward!!.animate().y(0f).setDuration(300).start()
                    backward!!.animate().y(0f).setDuration(300).start()
                    close!!.animate().x(displayOffsetX).setDuration(200).start()
                    manageList!!.animate().y(displayOffsetY).setDuration(200).start()
                }
            }
        })

        database.shortcutDao().getAllShortcuts().observe(this) { mainList ->
            val plusShortcut = Shortcut(
                getString(R.string.addShortcut), "",
                ContextCompat.getDrawable(this, R.drawable.ic_baseline_add_box_24)!!
                    .getBitmap()!!.toByteArray()
            )
            var tempList: ArrayList<Shortcut> = ArrayList()
            val newList: ArrayList<ArrayList<Shortcut>> = ArrayList()

            if (mainList.isNotEmpty()) {
                for (i in mainList) {
                    if (tempList.size == 20) {
                        newList.add(tempList)
                        tempList = ArrayList()
                    } else tempList.add(i)
                }
                if (tempList.isNotEmpty()) newList.add(tempList)
                newList[newList.lastIndex].add(plusShortcut)
                binding.recyclerInclude.mainRecycler.adapter =
                    ShortcutsPagerRecyclerAdapter(this, newList)

                pagerShapHelper.attachToRecyclerView(null)
                pagerShapHelper.attachToRecyclerView(binding.recyclerInclude.mainRecycler)
            } else {
                tempList.add(plusShortcut)
                newList.add(tempList)
                binding.recyclerInclude.mainRecycler.adapter =
                    ShortcutsPagerRecyclerAdapter(this, newList)
            }
            binding.indicator.attachToRecyclerView(binding.recyclerInclude.mainRecycler)
        }

        recursiveBottomNavigationClick()
    }

    private fun recursiveBottomNavigationClick() {
        history?.setOnClickListener {
            HistoryFragment().show(supportFragmentManager, "custom")
            delayBeforeNextClick()
        }
        currentWindows?.setOnClickListener {
            CurrentWindowsFragment().show(supportFragmentManager, "windows")
            delayBeforeNextClick()
        }
        bookmarks?.setOnClickListener {
            BookmarksFragment().show(supportFragmentManager, "custom")
            delayBeforeNextClick()
        }
        settings?.setOnClickListener {
            SettingsFragment().show(supportFragmentManager, "custom")
            delayBeforeNextClick()
        }
    }

    private fun delayBeforeNextClick() {
        clearListeners()
        Handler(mainLooper).postDelayed({
            recursiveBottomNavigationClick()
        }, 300)
    }

    private fun clearListeners() {
        history?.setOnClickListener {}
        currentWindows?.setOnClickListener {}
        bookmarks?.setOnClickListener {}
        settings?.setOnClickListener {}
    }

    private fun recursiveClickForward() {
        forward!!.setOnClickListener {
            val newPos = layoutManager!!.findLastCompletelyVisibleItemPosition() + 1
            if (newPos == toolbarAdapter!!.itemCount) toolbarRecycler!!.scrollToPosition(0)
            else toolbarRecycler!!.smoothScrollToPosition(newPos)
            it.setOnClickListener {}
            Handler(mainLooper).postDelayed({ recursiveClickForward() }, 200)
        }
    }

    private fun recursiveClickBackward() {
        backward!!.setOnClickListener {
            val newPos = layoutManager!!.findLastCompletelyVisibleItemPosition() - 1
            if (newPos == -1) toolbarRecycler!!.scrollToPosition(toolbarAdapter!!.itemCount)
            else toolbarRecycler!!.smoothScrollToPosition(newPos)
            backward!!.setOnClickListener {}
            Handler(mainLooper).postDelayed({
                recursiveClickBackward()
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

    override fun onBackPressed() {
        if (card?.translationY == 0f) {
            card?.animate()?.y(displayOffsetY)?.setDuration(350)?.withEndAction {
                labelRecycler?.adapter = null
                fab?.show()
                card?.visibility = View.GONE
            }?.start()
            forward?.animate()?.y(0f)?.setDuration(300)?.start()
            backward?.animate()?.y(0f)?.setDuration(300)?.start()
            close?.animate()?.x(displayOffsetX)?.setDuration(200)?.start()
            manageList?.animate()?.y(displayOffsetY)?.setDuration(200)?.start()
        }
    }

    override fun onEndList() {}

    override fun onStartList(labelList: ArrayList<String>) {
        toolbarAdapter?.labelList = labelList
        toolbarAdapter?.notifyDataSetChanged()
        toolbarAdapter?.labelListAdapter?.labelList = labelList
        toolbarAdapter?.labelListAdapter?.notifyDataSetChanged()
    }

    override fun onPause() {
        for (frag in supportFragmentManager.fragments) {
            supportFragmentManager.beginTransaction().remove(frag).commit()
        }
        super.onPause()
    }

}