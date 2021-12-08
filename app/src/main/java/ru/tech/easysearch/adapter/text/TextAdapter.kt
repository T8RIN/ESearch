package ru.tech.easysearch.adapter.text

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.tech.easysearch.R


class TextAdapter(
    private var text: String? = null,
    private var textCount: List<String> = listOf("_")
) :
    RecyclerView.Adapter<TextAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.text_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (text != null) holder.text.text = text
        else holder.text.text = textCount[position]
    }

    override fun getItemCount(): Int {
        return textCount.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.text)
    }

}
