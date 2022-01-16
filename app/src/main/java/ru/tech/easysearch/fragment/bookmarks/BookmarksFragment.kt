package ru.tech.easysearch.fragment.bookmarks

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.tech.easysearch.R
import ru.tech.easysearch.adapter.bookmark.BookmarksAdapter
import ru.tech.easysearch.application.ESearchApplication.Companion.database
import ru.tech.easysearch.custom.popup.smart.SmartPopupMenu
import ru.tech.easysearch.custom.popup.smart.SmartPopupMenuItem
import ru.tech.easysearch.data.SharedPreferencesAccess.loadTheme
import ru.tech.easysearch.databinding.BookmarksFragmentBinding
import ru.tech.easysearch.fragment.dialog.BookmarkCreationDialog
import ru.tech.easysearch.functions.Functions

class BookmarksFragment(private val browser: WebView? = null) : DialogFragment() {

    private var _binding: BookmarksFragmentBinding? = null
    private val binding get() = _binding!!

    private var menu: SmartPopupMenu? = null

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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireActivity(), theme) {
            override fun onBackPressed() {
                if (menu?.isHidden == false) menu?.dismiss()
                else dismiss()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, loadTheme(requireContext()))
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        requireDialog().window?.setWindowAnimations(
            R.style.DialogAnimation
        )

        binding.close.setOnClickListener { dismiss() }

        binding.more.setOnClickListener { showMore(requireActivity()) }

        database.bookmarkDao().getAllBookmarks().observe(this) {
            if (it.isNotEmpty()) {
                binding.errorMessage.visibility = GONE
                binding.bookmarkRecycler.visibility = VISIBLE
                binding.bookmarkRecycler.adapter =
                    BookmarksAdapter(this@BookmarksFragment, it, browser)
            } else {
                binding.errorMessage.visibility = VISIBLE
                binding.bookmarkRecycler.visibility = GONE
            }
        }

    }

    private fun showMore(context: FragmentActivity) {
        menu = SmartPopupMenu(binding.root.parent as ViewGroup, context)
            .addItems(
                SmartPopupMenuItem(
                    R.drawable.ic_baseline_add_box_24,
                    ContextCompat.getDrawable(context, R.drawable.ic_baseline_add_box_24),
                    getString(R.string.newBookmark)
                ),
                SmartPopupMenuItem(
                    R.drawable.ic_baseline_delete_sweep_24,
                    ContextCompat.getDrawable(context, R.drawable.ic_baseline_delete_sweep_24),
                    getString(R.string.clearBookmarks)
                )
            )
            .setMenuItemClickListener {
                when (it.id) {
                    R.drawable.ic_baseline_add_box_24 -> {
                        BookmarkCreationDialog("", "").show(
                            context.supportFragmentManager,
                            "bookmarkCreation"
                        )
                    }
                    R.drawable.ic_baseline_delete_sweep_24 -> {
                        if (binding.bookmarkRecycler.adapter?.itemCount != 0 && binding.bookmarkRecycler.adapter != null) {
                            MaterialAlertDialogBuilder(context)
                                .setTitle(R.string.clearBookmarks)
                                .setMessage(R.string.clearBookmarksMessage)
                                .setPositiveButton(R.string.ok_ok) { _, _ ->
                                    Functions.doInBackground {
                                        database.bookmarkDao().deleteAllBookmarks()
                                    }
                                    dismiss()
                                }
                                .setNegativeButton(R.string.cancel, null)
                                .show()
                        } else {
                            Toast.makeText(
                                context.applicationContext,
                                R.string.noBookmarksClear,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                }
                menu?.dismiss()
            }

        menu!!.show()
    }

}