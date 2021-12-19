package ru.tech.easysearch.adapter.text

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.tech.easysearch.databinding.TextItemBinding


class TextAdapter(
    private var text: String? = null,
    private var textCount: List<String> = listOf("_")
) :
    RecyclerView.Adapter<TextAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            TextItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (text != null) holder.text.text = text
        else holder.text.text = textCount[position]
    }

    override fun getItemCount(): Int {
        return textCount.size
    }

    inner class ViewHolder(binding: TextItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val text: TextView = binding.text
    }

}
