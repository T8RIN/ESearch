package ru.tech.easysearch.adapter.selection

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ru.tech.easysearch.R
import ru.tech.easysearch.databinding.SelectableLabelItemBinding
import ru.tech.easysearch.extensions.Extensions.getResId
import ru.tech.easysearch.extensions.Extensions.setTint
import ru.tech.easysearch.helper.interfaces.LabelListChangedInterface


class DeSelectedLabelsAdapter(
    private val context: Context,
    var labelList: ArrayList<String>,
    private var labelListChangedInterface: LabelListChangedInterface
) :
    RecyclerView.Adapter<DeSelectedLabelsAdapter.ViewHolder>() {

    private var selectedAdapter: SelectedLabelsAdapter? = null

    fun bindAdapter(adapter: SelectedLabelsAdapter) {
        selectedAdapter = adapter
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            SelectableLabelItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.label.setImageResource(R.drawable::class.java.getResId(labelList[position]))
        holder.icon.setImageResource(R.drawable.ic_baseline_add_circle_24)
        holder.icon.setTint(ContextCompat.getColor(context, R.color.dgreen))
        holder.dragger.visibility = GONE
        holder.icon.setOnClickListener {
            selectedAdapter?.labelList!!.add(labelList[position])
            selectedAdapter?.notifyItemInserted(selectedAdapter!!.labelList.size)
            labelList.removeAt(position)
            if (labelList.isEmpty()) labelListChangedInterface.onEndList()
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return labelList.size
    }

    inner class ViewHolder(binding: SelectableLabelItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val label: ImageView = binding.label
        val icon: ImageButton = binding.icon
        val dragger: ImageView = binding.dragger
    }

}
