package ru.tech.easysearch.fragment.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.DialogFragment
import ru.tech.easysearch.R
import ru.tech.easysearch.adapter.history.HistoryAdapter
import ru.tech.easysearch.application.ESearchApplication.Companion.database
import ru.tech.easysearch.custom.StickyHeaderDecoration
import ru.tech.easysearch.databinding.HistoryFragmentBinding

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
        database.historyDao().getHistory().observe(this) {
            if (it.isNotEmpty()) {
                val booleanArray: ArrayList<Boolean> = ArrayList()
                var prev = ""
                for (i in it) {
                    if (i.date != prev) {
                        booleanArray.add(true)
                        prev = i.date
                    } else {
                        booleanArray.add(false)
                    }
                }
                val adapter = HistoryAdapter(this@HistoryFragment, it, booleanArray, browser)

                binding.historyRecycler.adapter = adapter
                binding.historyRecycler.addItemDecoration(StickyHeaderDecoration(adapter))

            } else {
                binding.errorMessage.visibility = View.VISIBLE
                binding.historyRecycler.visibility = View.GONE
            }
        }
    }


}