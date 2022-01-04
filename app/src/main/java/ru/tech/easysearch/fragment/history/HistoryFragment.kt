package ru.tech.easysearch.fragment.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.tech.easysearch.R
import ru.tech.easysearch.adapter.history.HistoryAdapter
import ru.tech.easysearch.application.ESearchApplication.Companion.database
import ru.tech.easysearch.custom.stickyheader.StickyHeaderDecoration
import ru.tech.easysearch.database.hist.History
import ru.tech.easysearch.databinding.HistoryFragmentBinding
import ru.tech.easysearch.functions.Functions.doInBackground
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HistoryFragment(private val browser: WebView? = null) : DialogFragment() {

    private var _binding: HistoryFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HistoryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        requireDialog().window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.Theme_ESearch)
    }

    private var adapter: HistoryAdapter? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        requireDialog().window?.setWindowAnimations(
            R.style.DialogAnimation
        )
        binding.close.setOnClickListener {
            dismiss()
        }
        database.historyDao().getHistory().observe(this) { liveList ->
            if (liveList.isNotEmpty()) {
                CoroutineScope(Dispatchers.Main).launch {
                    binding.indicator.root.visibility = VISIBLE
                    computeList(liveList)
                    adapter =
                        HistoryAdapter(this@HistoryFragment, historyList, booleanArray, browser)
                    binding.indicator.root.visibility = GONE
                    binding.historyRecycler.adapter = adapter!!
                    binding.historyRecycler.addItemDecoration(StickyHeaderDecoration(adapter!!))

                }
            } else {
                binding.errorMessage.visibility = VISIBLE
                binding.historyRecycler.visibility = GONE
            }
        }

        binding.clear.setOnClickListener {
            if (adapter?.itemCount != 0) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.clearHistory)
                    .setMessage(R.string.clearHistoryMessage)
                    .setPositiveButton(R.string.ok_ok) { _, _ ->
                        doInBackground {
                            database.historyDao().clearHistory()
                        }
                        dismiss()
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            } else {
                Toast.makeText(
                    requireContext().applicationContext,
                    R.string.noHistoryClear,
                    Toast.LENGTH_SHORT
                )
                    .show()
            }

        }

    }

    val booleanArray: ArrayList<Boolean> = ArrayList()
    val historyList: ArrayList<History> = ArrayList()

    private suspend fun computeList(liveList: List<History>) = withContext(Dispatchers.IO) {
        booleanArray.clear()
        historyList.clear()

        val sortedList = ArrayList(liveList)
        sortedList.sortByDescending {
            LocalDateTime.parse(
                it.sortingString,
                DateTimeFormatter.ofPattern("dd-MM-yyyy | HH:mm")
            )
        }

        var prev = ""
        for (i in sortedList) {
            var needToAddMore = false
            if (i.date != prev) {
                booleanArray.add(true)
                historyList.add(i)
                prev = i.date
                needToAddMore = true
            } else {
                booleanArray.add(false)
            }
            historyList.add(i)
            if (needToAddMore) booleanArray.add(false)
        }
    }


}