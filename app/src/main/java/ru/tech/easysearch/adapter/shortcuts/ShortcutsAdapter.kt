package ru.tech.easysearch.adapter.shortcuts

import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import ru.tech.easysearch.R
import ru.tech.easysearch.activity.BrowserActivity
import ru.tech.easysearch.activity.MainActivity
import ru.tech.easysearch.application.ESearchApplication.Companion.coeff
import ru.tech.easysearch.application.ESearchApplication.Companion.database
import ru.tech.easysearch.custom.popup.simple.SimplePopupBuilder
import ru.tech.easysearch.custom.popup.simple.SimplePopupBuilder.Companion.COPY
import ru.tech.easysearch.custom.popup.simple.SimplePopupBuilder.Companion.DELETE
import ru.tech.easysearch.custom.popup.simple.SimplePopupBuilder.Companion.EDIT
import ru.tech.easysearch.custom.popup.simple.SimplePopupBuilder.Companion.SHARE
import ru.tech.easysearch.custom.popup.simple.SimplePopupClickListener
import ru.tech.easysearch.custom.popup.simple.SimplePopupItem
import ru.tech.easysearch.database.shortcuts.Shortcut
import ru.tech.easysearch.databinding.MainGridItemBinding
import ru.tech.easysearch.extensions.Extensions.getAttrColor
import ru.tech.easysearch.extensions.Extensions.makeClip
import ru.tech.easysearch.extensions.Extensions.shareWith
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
        Glide.with(context.applicationContext).load(byteArrayToBitmap(shortcut.icon!!))
            .into(holder.icon)

        holder.description.text = shortcut.description
        if (position != newList.size - 1 || !isLast) {
            holder.itemView.setOnClickListener {
                val intent = Intent(context, BrowserActivity::class.java)
                intent.putExtra("url", shortcut.url)
                context.startActivity(intent)
            }

            holder.itemView.setOnLongClickListener {
                SimplePopupBuilder(context, it)
                    .setMenuClickListener(SimplePopupClickListener { id ->
                        when (id) {
                            EDIT -> {
                                val shortcutEdit =
                                    ShortcutCreationDialog(
                                        shortcut.url,
                                        shortcut.description,
                                        true,
                                        shortcut.id
                                    )
                                if (!shortcutEdit.isAdded) shortcutEdit.show(
                                    (context as AppCompatActivity).supportFragmentManager,
                                    "shortcut_edition"
                                )
                            }
                            SHARE -> context.shareWith(shortcut.url)
                            COPY -> context.makeClip(shortcut.url)
                            DELETE -> {
                                Snackbar.make(
                                    (context as MainActivity).binding.root,
                                    shortcut.url,
                                    Snackbar.LENGTH_LONG
                                )
                                    .setBackgroundTint(
                                        ContextCompat.getColor(
                                            context,
                                            R.color.materialGray
                                        )
                                    )
                                    .setAction(R.string.undo) {
                                        doInBackground { database.shortcutDao().insert(shortcut) }
                                    }
                                    .setActionTextColor(
                                        context.getAttrColor(R.attr.colorSecondary)
                                    )
                                    .setAnchorView((context as MainActivity).binding.fab)
                                    .setTextColor(ContextCompat.getColor(context, R.color.white))
                                    .show()
                                doInBackground { database.shortcutDao().delete(shortcut) }
                            }
                        }
                    })
                    .addItems(
                        SimplePopupItem(
                            EDIT,
                            R.string.edit,
                            R.drawable.ic_baseline_edit_24
                        ),
                        SimplePopupItem(
                            SHARE,
                            R.string.share,
                            R.drawable.ic_baseline_share_24
                        ),
                        SimplePopupItem(
                            COPY,
                            R.string.copy,
                            R.drawable.ic_baseline_content_copy_24
                        ),
                        SimplePopupItem(
                            DELETE,
                            R.string.delete,
                            R.drawable.ic_baseline_delete_sweep_24
                        ),
                    )
                    .show()
                true
            }

        } else if (isLast) {
            holder.itemView.setOnClickListener {
                val shortcutDialog = ShortcutCreationDialog()
                if (!shortcutDialog.isAdded) shortcutDialog.show(
                    (context as AppCompatActivity).supportFragmentManager,
                    "shortcutDialog"
                )
            }
            holder.itemView.setOnLongClickListener { true }
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
