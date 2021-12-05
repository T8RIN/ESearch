package ru.tech.easysearch.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import ru.tech.easysearch.R


class ToolbarAdapter(
    var context: Context,
    var labelList: List<Int>
) :
    RecyclerView.Adapter<ToolbarAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.toolbar_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.label.setImageResource(labelList[position])
    }

    override fun getItemCount(): Int {
        return labelList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val label: ImageView = view.findViewById(R.id.label)
    }

}
