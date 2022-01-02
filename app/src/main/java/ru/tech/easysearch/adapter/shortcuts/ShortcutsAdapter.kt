package ru.tech.easysearch.adapter.shortcuts

import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import ru.tech.easysearch.R
import ru.tech.easysearch.activity.BrowserActivity
import ru.tech.easysearch.application.ESearchApplication.Companion.coeff
import ru.tech.easysearch.application.ESearchApplication.Companion.database
import ru.tech.easysearch.database.shortcuts.Shortcut
import ru.tech.easysearch.databinding.MainGridItemBinding
import ru.tech.easysearch.fragment.dialog.ShortcutCreationDialog
import ru.tech.easysearch.functions.Functions.byteArrayToBitmap
import ru.tech.easysearch.functions.Functions.doInBackground

class ShortcutsAdapter(
    private var context: Context,
    private var newList: List<Shortcut>,
    private val isLast: Boolean
) :
    RecyclerView.Adapter<ShortcutsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(MainGridItemBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val shortcut = newList[position]
        holder.icon.setImageBitmap(byteArrayToBitmap(shortcut.icon!!))
        holder.description.text = shortcut.description
        holder.itemView.setOnClickListener {
            if (position != newList.size - 1 || !isLast) {
                val intent = Intent(context, BrowserActivity::class.java)
                intent.putExtra("url", shortcut.url)
                context.startActivity(intent)
            } else if (isLast) {
                val shortcutDialog = ShortcutCreationDialog()
                if (!shortcutDialog.isAdded) shortcutDialog.show(
                    (context as AppCompatActivity).supportFragmentManager,
                    "shortcutDialog"
                )
            }
        }
        holder.itemView.setOnLongClickListener {
            val menu = PopupMenu(context, it)
            menu.setForceShowIcon(true)
            menu.menu.add(0, 1, 0, R.string.delete).setIcon(R.drawable.ic_baseline_delete_sweep_24)
            menu.setOnMenuItemClickListener { item ->
                if (item.itemId == 1) {
                    doInBackground { database.shortcutDao().delete(shortcut) }
                }
                true
            }
            menu.show()
            true
        }
        holder.itemView.layoutParams.height =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                coeff.toFloat(),
                context.resources.displayMetrics
            )
                .toInt()
    }

    override fun getItemCount(): Int {
        return newList.size
    }

    inner class ViewHolder(binding: MainGridItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val icon = binding.icon
        val description = binding.description
    }

}
