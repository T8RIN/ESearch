package ru.tech.easysearch.adapter.selection

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.tech.easysearch.R
import ru.tech.easysearch.databinding.SelectableLabelItemBinding
import ru.tech.easysearch.extensions.Extensions.getResId
import ru.tech.easysearch.extensions.Extensions.setTint
import ru.tech.easysearch.helper.interfaces.LabelListChangedInterface


class SelectedLabelsAdapter(
    private val context: Context,
    var labelList: ArrayList<String>,
    private var disAdapter: DeSelectedLabelsAdapter,
    private val labelListChangedInterface: LabelListChangedInterface
) :
    RecyclerView.Adapter<SelectedLabelsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            SelectableLabelItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context).load(labelList[position].getResId()).into(holder.label)
        Glide.with(context).load(R.drawable.ic_baseline_remove_circle_24).into(holder.icon)

        holder.icon.setTint(ContextCompat.getColor(context, R.color.red))
        holder.icon.setOnClickListener {
            if (labelList.size > 2) {
                disAdapter.labelList.add(labelList[position])
                disAdapter.notifyItemInserted(disAdapter.labelList.size - 1)
                labelList.removeAt(position)
                if (disAdapter.labelList.size == 1) labelListChangedInterface.onStartList(labelList)
                notifyDataSetChanged()
            } else {
                Toast.makeText(context, R.string.minIsTwo, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return labelList.size
    }

    inner class ViewHolder(binding: SelectableLabelItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val label: ImageView = binding.label
        val icon: ImageButton = binding.icon
    }

}
