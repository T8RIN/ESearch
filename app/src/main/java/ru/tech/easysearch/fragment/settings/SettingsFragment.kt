package ru.tech.easysearch.fragment.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import ru.tech.easysearch.R
import ru.tech.easysearch.adapter.settings.BrowserSettingsAdapter
import ru.tech.easysearch.databinding.SettingsFragmentBinding
import ru.tech.easysearch.extensions.Extensions.createSettingsList

class SettingsFragment : DialogFragment() {

    private var _binding: SettingsFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SettingsFragmentBinding.inflate(inflater, container, false)
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
        binding.settingsRecycler.adapter =
            BrowserSettingsAdapter(requireContext(), requireContext().createSettingsList())

    }

}