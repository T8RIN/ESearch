package ru.tech.easysearch.adapter.selection

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import ru.tech.easysearch.R
import ru.tech.easysearch.extensions.Extensions.setTint
import ru.tech.easysearch.helper.interfaces.LabelListChangedInterface


class SelectedLabelsAdapter(
    private val context: Context,
    var labelList: ArrayList<Int>,
    private var disAdapter: DeSelectedLabelsAdapter,
    private val labelListChangedInterface: LabelListChangedInterface
) :
    RecyclerView.Adapter<SelectedLabelsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.selectable_label_item, parent, false)
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.label.setImageResource(labelList[position])
        holder.icon.setImageResource(R.drawable.ic_baseline_remove_circle_24)
        holder.icon.setTint(R.color.red)
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

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val label: ImageView = view.findViewById(R.id.label)
        val icon: ImageButton = view.findViewById(R.id.icon)
    }

}
