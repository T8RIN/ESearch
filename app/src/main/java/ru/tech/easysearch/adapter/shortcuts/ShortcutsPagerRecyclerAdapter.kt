package ru.tech.easysearch.adapter.shortcuts

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.tech.easysearch.database.shortcuts.Shortcut
import ru.tech.easysearch.databinding.GridPagerItemBinding

class ShortcutsPagerRecyclerAdapter(
    private var context: Context,
    private var mainList: ArrayList<ArrayList<Shortcut>>
) :
    RecyclerView.Adapter<ShortcutsPagerRecyclerAdapter.ViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(GridPagerItemBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(position != mainList.size - 1) holder.mainBookmarksRecycler.adapter = ShortcutsAdapter(context, mainList[position], false)
        else holder.mainBookmarksRecycler.adapter = ShortcutsAdapter(context, mainList[position], true)
    }

    override fun getItemCount(): Int {
        return mainList.size
    }

    inner class ViewHolder(binding: GridPagerItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val mainBookmarksRecycler = binding.mainBookmarksRecycler
    }

}
