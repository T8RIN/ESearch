package ru.tech.easysearch.fragment.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.tech.easysearch.R
import ru.tech.easysearch.adapter.selection.DeSelectedLabelsAdapter
import ru.tech.easysearch.adapter.selection.RecyclerItemTouchAdapter
import ru.tech.easysearch.adapter.text.TextAdapter
import ru.tech.easysearch.data.DataArrays.prefixDict
import ru.tech.easysearch.data.SharedPreferencesAccess.loadLabelList
import ru.tech.easysearch.data.SharedPreferencesAccess.saveLabelList
import ru.tech.easysearch.databinding.SelectLabelsDialogBinding
import ru.tech.easysearch.helper.interfaces.LabelListChangedInterface

class SelectLabelsDialog(private val mainInterface: LabelListChangedInterface) :
    DialogFragment(), LabelListChangedInterface {

    private var _binding: SelectLabelsDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SelectLabelsDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        requireDialog().window?.setLayout(MATCH_PARENT, MATCH_PARENT)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.Theme_ESearch)
    }

    private var adapter: ConcatAdapter? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        requireDialog().window?.setWindowAnimations(
            R.style.DialogAnimation
        )

        val toolbar: Toolbar = binding.toolbar
        toolbar.setNavigationIcon(R.drawable.ic_baseline_close_24)
        toolbar.setTitle(R.string.selectSearchEngine)
        toolbar.setNavigationOnClickListener {
            requireDialog().dismiss()
        }

        val labelList: ArrayList<String> = ArrayList(loadLabelList(requireContext())!!.split("+"))
        val disLabelList: ArrayList<String> = ArrayList()

        for (i in prefixDict) if (!labelList.contains(i.key)) disLabelList.add(i.key)

        val recycler: RecyclerView = binding.labelSelectionRecycler
        val disAdapter = DeSelectedLabelsAdapter(requireContext(), disLabelList, this)
        val nestedAdapter = RecyclerItemTouchAdapter(requireContext(), labelList, disAdapter, this)

        adapter = if (disLabelList.isNotEmpty()) {
            ConcatAdapter(
                nestedAdapter,
                TextAdapter(getString(R.string.disabledService)),
                disAdapter
            )
        } else {
            ConcatAdapter(
                nestedAdapter,
                disAdapter
            )
        }

        recycler.adapter = adapter

        requireDialog().setOnDismissListener {
            val list = ((adapter!!.adapters[0] as RecyclerItemTouchAdapter).adapter.labelList)
            saveLabelList(requireContext(), list.joinToString("+"))
            mainInterface.onStartList(list)
            this.dismiss()
        }
    }

    override fun onEndList() {
        val labelList: ArrayList<String> = ArrayList()
        for (i in prefixDict) labelList.add(i.key)

        val recycler: RecyclerView = binding.labelSelectionRecycler
        val disAdapter = DeSelectedLabelsAdapter(requireContext(), ArrayList(), this)
        val nestedAdapter = RecyclerItemTouchAdapter(requireContext(), labelList, disAdapter, this)

        adapter = ConcatAdapter(
            nestedAdapter,
            disAdapter
        )

        recycler.adapter = adapter
    }

    override fun onStartList(labelList: ArrayList<String>) {
        val disLabelList: ArrayList<String> = ArrayList()
        for (i in prefixDict) if (!labelList.contains(i.key)) disLabelList.add(i.key)

        val recycler: RecyclerView = binding.labelSelectionRecycler
        val disAdapter = DeSelectedLabelsAdapter(requireContext(), disLabelList, this)
        val nestedAdapter = RecyclerItemTouchAdapter(requireContext(), labelList, disAdapter, this)

        adapter = ConcatAdapter(
            nestedAdapter,
            TextAdapter(getString(R.string.disabledService)),
            disAdapter
        )

        recycler.adapter = adapter
    }
}