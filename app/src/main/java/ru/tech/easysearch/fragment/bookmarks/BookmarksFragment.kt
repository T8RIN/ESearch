package ru.tech.easysearch.fragment.bookmarks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.DialogFragment
import ru.tech.easysearch.R
import ru.tech.easysearch.adapter.bookmark.BookmarksAdapter
import ru.tech.easysearch.application.ESearchApplication.Companion.database
import ru.tech.easysearch.databinding.BookmarksFragmentBinding

class BookmarksFragment(private val browser: WebView? = null) : DialogFragment() {

    private var _binding: BookmarksFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BookmarksFragmentBinding.inflate(inflater, container, false)
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

        val dao = database.bookmarkDao()

        binding.close.setOnClickListener{
            dismiss()
        }

        dao.getAllBookmarks().observe(this) {
            if (it.isNotEmpty()) {
                binding.bookmarkRecycler.adapter = BookmarksAdapter(this@BookmarksFragment, it, browser)
            }
            else {
                binding.errorMessage.visibility = VISIBLE
                binding.bookmarkRecycler.visibility = GONE
            }
        }
    }

}