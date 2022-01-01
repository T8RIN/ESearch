package ru.tech.easysearch.fragment.current

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.tech.easysearch.R
import ru.tech.easysearch.activity.BrowserActivity
import ru.tech.easysearch.activity.MainActivity
import ru.tech.easysearch.adapter.tabs.TabAdapter
import ru.tech.easysearch.data.BrowserTabItem
import ru.tech.easysearch.data.BrowserTabs.createNewTab
import ru.tech.easysearch.data.BrowserTabs.loadTab
import ru.tech.easysearch.data.BrowserTabs.openedTabs
import ru.tech.easysearch.data.BrowserTabs.saveLastTab
import ru.tech.easysearch.data.BrowserTabs.updateTabs
import ru.tech.easysearch.databinding.CurrentWindowsFragmentBinding

class CurrentWindowsFragment : DialogFragment() {

    private var _binding: CurrentWindowsFragmentBinding? = null
    val binding get() = _binding!!

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

    private var needToLoad = false
    private var position = 0

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        requireDialog().window?.setWindowAnimations(
            R.style.DialogAnimation
        )
        val activity = requireActivity()
        requireDialog().setOnDismissListener {
            if(needToLoad) (activity as? BrowserActivity)?.loadTab(position, false)
            activity.updateTabs()
        }
        (activity as? BrowserActivity)?.saveLastTab()

        binding.close.setOnClickListener { requireDialog().dismiss() }

        binding.clear.setOnClickListener {
            if (openedTabs.isNotEmpty()) {
                MaterialAlertDialogBuilder(activity)
                    .setTitle(R.string.clearTabs)
                    .setMessage(R.string.clearTabsMessage)
                    .setPositiveButton(R.string.ok_ok) { _, _ ->
                        if (activity is BrowserActivity) activity.finish()
                        else requireDialog().dismiss()
                        openedTabs.clear()
                        requireContext().updateTabs()
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            } else {
                Toast.makeText(activity.applicationContext, R.string.noTabs, Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.addTab.setOnClickListener {
            requireDialog().dismiss()
            if (activity is MainActivity) {
                val intent = Intent(activity, BrowserActivity::class.java)
                intent.putExtra("url", "https://google.com")
                requireContext().startActivity(intent)
            } else (activity as? BrowserActivity)?.createNewTab()
        }

        binding.label.apply {
            text =
                if (openedTabs.isNotEmpty()) "${getString(R.string.tabsOpened)} ${openedTabs.size}"
                else getString(R.string.tabs)
        }

        binding.tabRecycler.apply {
            val list: ArrayList<BrowserTabItem> = ArrayList()
            list.addAll(openedTabs)
            adapter = TabAdapter(requireContext(), list, this@CurrentWindowsFragment)
        }
    }

    fun notifyPosition(position: Int){
        needToLoad = true
        this.position = position
    }

}
