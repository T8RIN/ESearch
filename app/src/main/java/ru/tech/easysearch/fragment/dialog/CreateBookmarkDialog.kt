package ru.tech.easysearch.fragment.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import ru.tech.easysearch.R
import ru.tech.easysearch.application.ESearchApplication
import ru.tech.easysearch.database.bookmarks.Bookmark
import ru.tech.easysearch.databinding.CreateBookmarkDialogBinding
import ru.tech.easysearch.functions.Functions.doInBackground

class CreateBookmarkDialog(
    private val description: String? = "",
    private val url: String,
    private val array: ByteArray
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
        setStyle(STYLE_NO_FRAME, R.style.Theme_ESearch)
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
            doInBackground {
                dao.insert(
                    Bookmark(
                        binding.description.editText!!.text.toString(),
                        binding.url.editText!!.text.toString(),
                        array
                    )
                )
            }
            Toast.makeText(requireContext(), R.string.bookmarkAdded, Toast.LENGTH_SHORT).show()
            dismiss()
        }
        binding.close.setOnClickListener {
            dismiss()
        }
    }

}
