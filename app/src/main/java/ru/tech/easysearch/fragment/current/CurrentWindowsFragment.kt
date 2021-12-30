package ru.tech.easysearch.fragment.current

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import ru.tech.easysearch.R
import ru.tech.easysearch.activity.BrowserActivity
import ru.tech.easysearch.activity.MainActivity
import ru.tech.easysearch.adapter.tabs.TabAdapter
import ru.tech.easysearch.data.BrowserTabItem
import ru.tech.easysearch.data.BrowserTabs.createNewTab
import ru.tech.easysearch.data.BrowserTabs.saveLastTab
import ru.tech.easysearch.data.BrowserTabs.openedTabs
import ru.tech.easysearch.databinding.CurrentWindowsFragmentBinding

class CurrentWindowsFragment : DialogFragment() {

    private var _binding: CurrentWindowsFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CurrentWindowsFragmentBinding.inflate(inflater, container, false)
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
        val activity = requireActivity()
        (activity as? BrowserActivity)?.saveLastTab()

        binding.close.setOnClickListener{ dismiss() }


        binding.addTab.setOnClickListener{
            dismiss()
            if(activity is MainActivity){
                val intent = Intent(activity, BrowserActivity::class.java)
                intent.putExtra("url", "https://google.com")
                requireContext().startActivity(intent)
            }
            else (activity as? BrowserActivity)?.createNewTab()
        }

        binding.tabRecycler.apply{
            val list: ArrayList<BrowserTabItem> = ArrayList()
            list.addAll(openedTabs)
            adapter = TabAdapter(requireContext(), list, this@CurrentWindowsFragment)
        }

    }

}
