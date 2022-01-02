package ru.tech.easysearch.fragment.results

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.*
import android.widget.ImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bekawestberg.loopinglayout.library.LoopingLayoutManager
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import ru.tech.easysearch.R
import ru.tech.easysearch.activity.BrowserActivity
import ru.tech.easysearch.activity.MainActivity
import ru.tech.easysearch.adapter.toolbar.ToolbarAdapter
import ru.tech.easysearch.custom.view.BrowserView
import ru.tech.easysearch.data.DataArrays
import ru.tech.easysearch.data.SharedPreferencesAccess
import ru.tech.easysearch.databinding.SearchResultsFragmentBinding
import ru.tech.easysearch.extensions.Extensions.hideKeyboard
import ru.tech.easysearch.fragment.dialog.SelectLabelsDialog
import ru.tech.easysearch.functions.Functions.delayedDoInForeground
import ru.tech.easysearch.helper.client.ChromeClient
import ru.tech.easysearch.helper.client.WebClient
import ru.tech.easysearch.helper.interfaces.LabelListChangedInterface

class SearchResultsFragment : DialogFragment(), LabelListChangedInterface {

    private var _binding: SearchResultsFragmentBinding? = null
    private val binding get() = _binding!!

    private var searchView: SearchView? = null
    private var toolbarAdapter: ToolbarAdapter? = null
    private var layoutManager: LoopingLayoutManager? = null
    var card: MaterialCardView? = null
    private var manageList: ImageButton? = null
    private var close: ImageButton? = null
    private var backButton: ImageButton? = null
    private var progressBar: LinearProgressIndicator? = null
    var browser: BrowserView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SearchResultsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        requireDialog().window?.setLayout(
            MATCH_PARENT,
            MATCH_PARENT
        )
    }

    var backpressed = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireActivity(), theme) {
            override fun onBackPressed() {
                if (backpressed) {
                    backpressed = false
                    requireDialog().dismiss()
                } else if (card?.translationY == 0f) {
                    hideSearchSelectionCard()
                } else requireDialog().dismiss()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.Theme_ESearch)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        val activity: MainActivity = requireActivity() as MainActivity
        val toolbarRecycler: RecyclerView = binding.appBar.toolbarRecycler

        requireDialog().window?.setWindowAnimations(
            R.style.DialogAnimation
        )
        requireDialog().setOnDismissListener {
            layoutManager?.findFirstCompletelyVisibleItemPosition()
                ?.let { position -> activity.scrollToPosition(position) }
            activity.onStartList(
                ArrayList(SharedPreferencesAccess.loadLabelList(activity)!!.split("+"))
            )
        }

        searchView = binding.searchView
        progressBar = binding.appBar.progressIndicator
        browser = binding.webBrowser

        browser!!.webViewClient = WebClient(requireActivity(), progressBar!!)
        browser!!.webChromeClient = ChromeClient(requireActivity(), progressBar!!, browser!!)

        prefix = arguments?.get("prefix").toString()
        val query = arguments?.get("url").toString().removePrefix(prefix)
        onGetUri(query)

        MainActivity.displayOffsetY = -resources.displayMetrics.heightPixels.toFloat()
        MainActivity.displayOffsetX = -resources.displayMetrics.widthPixels.toFloat()

        backButton = binding.backButton

        val labelList: ArrayList<String> =
            ArrayList(SharedPreferencesAccess.loadLabelList(activity)!!.split("+"))

        layoutManager = LoopingLayoutManager(
            activity,
            LoopingLayoutManager.HORIZONTAL,
            false
        )
        toolbarRecycler.layoutManager = layoutManager

        card = binding.labelSuggestionCard
        card!!.translationY = MainActivity.displayOffsetY
        manageList = binding.manageList
        manageList!!.translationY = MainActivity.displayOffsetY
        close = binding.closeButton
        close!!.translationX = MainActivity.displayOffsetX

        val labelRecycler: RecyclerView = binding.labelRecycler

        toolbarAdapter = ToolbarAdapter(
            activity,
            labelList,
            card!!,
            null,
            labelRecycler,
            toolbarRecycler,
            null,
            null,
            manageList!!,
            close!!,
            backButton!!
        )
        toolbarRecycler.adapter = toolbarAdapter

        layoutManager!!.scrollToPosition(
            toolbarAdapter!!.labelList.indexOf(
                DataArrays.prefixDict.filterValues { it == prefix }.keys.elementAt(0)
            )
        )

        val selectLabelsFragment = SelectLabelsDialog(this)
        manageList!!.setOnClickListener {
            if (!selectLabelsFragment.isAdded) selectLabelsFragment.show(
                activity.supportFragmentManager,
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
        helper.attachToRecyclerView(toolbarRecycler)

        var animating = false

        toolbarRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!animating && card!!.translationY == 0f) {
                    animating = true
                    hideSearchSelectionCard()
                    Handler(activity.mainLooper).postDelayed({ animating = false }, 350)
                }
                delayedDoInForeground(300) {
                    val key =
                        layoutManager?.findLastCompletelyVisibleItemPosition()
                            ?.let { toolbarAdapter?.labelList?.get(it) }
                    prefix = DataArrays.prefixDict[key]!!
                    if (uriLast.isNotEmpty()) onGetUri(uriLast)
                    searchView?.clearFocus()
                    browser?.hideKeyboard(activity)
                }
            }
        })

        close!!.setOnClickListener {
            hideSearchSelectionCard()
        }

        backButton!!.setOnClickListener {
            backpressed = true
            requireDialog().onBackPressed()
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
            val intent = Intent(requireActivity(), BrowserActivity::class.java)
            intent.putExtra("url", tempUrl)
            startActivity(intent)
        } else {
            browser?.loadUrl(prefix + uriLast)
        }

    }

    private var uriLast: String = ""

    override fun onEndList() {}

    @SuppressLint("NotifyDataSetChanged")
    override fun onStartList(labelList: ArrayList<String>) {
        toolbarAdapter?.labelList = labelList
        toolbarAdapter?.notifyDataSetChanged()
        toolbarAdapter?.labelListAdapter?.labelList = labelList
        toolbarAdapter?.labelListAdapter?.notifyDataSetChanged()
    }

    fun hideSearchSelectionCard() {
        card?.animate()?.y(MainActivity.displayOffsetY)?.setDuration(350)?.withEndAction {
            card?.visibility = View.GONE
        }?.start()
        close?.animate()?.x(MainActivity.displayOffsetX)?.setDuration(200)?.start()
        manageList?.animate()?.y(MainActivity.displayOffsetY)?.setDuration(200)?.start()
        backButton?.animate()?.y(0f)?.setDuration(200)?.start()
    }

}