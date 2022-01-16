package ru.tech.easysearch.fragment.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import ru.tech.easysearch.R
import ru.tech.easysearch.application.ESearchApplication
import ru.tech.easysearch.data.SharedPreferencesAccess.loadTheme
import ru.tech.easysearch.database.bookmarks.Bookmark
import ru.tech.easysearch.databinding.CreateBookmarkDialogBinding
import ru.tech.easysearch.extensions.Extensions.fetchFavicon
import ru.tech.easysearch.extensions.Extensions.toByteArray
import ru.tech.easysearch.functions.Functions.doInBackground

class BookmarkCreationDialog(
    private val url: String,
    private val description: String,
    private val editing: Boolean = false,
    private val uid: Int? = 0
) : DialogFragment() {

    private var _binding: CreateBookmarkDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CreateBookmarkDialogBinding.inflate(inflater, container, false)
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
        setStyle(STYLE_NO_FRAME, loadTheme(requireContext()))
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        requireDialog().window?.setWindowAnimations(
            R.style.DialogAnimation
        )
        binding.description.editText?.setText(description)
        binding.url.editText?.setText(url)
        binding.done.setOnClickListener {
            val dao = ESearchApplication.database.bookmarkDao()
            val newUrl = binding.url.editText!!.text.toString().trim()
            val title = binding.description.editText!!.text.toString().trim()
            if (title != "" && newUrl != "") {
                if (!editing) {
                    doInBackground {
                        val icon = requireContext().fetchFavicon(newUrl).toByteArray()
                        dao.insert(Bookmark(title, newUrl, icon))
                    }
                    Toast.makeText(requireContext(), R.string.bookmarkAdded, Toast.LENGTH_SHORT)
                        .show()
                } else {
                    doInBackground {
                        val icon = requireContext().fetchFavicon(newUrl).toByteArray()
                        dao.update(Bookmark(title, newUrl, icon, uid))
                    }
                    Toast.makeText(requireContext(), R.string.bookmarkSaved, Toast.LENGTH_SHORT)
                        .show()
                }
                dismiss()
            } else {
                Toast.makeText(requireContext(), R.string.fillAllFields, Toast.LENGTH_SHORT).show()
            }

        }
        binding.close.setOnClickListener {
            dismiss()
        }
    }

}
