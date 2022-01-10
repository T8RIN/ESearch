package ru.tech.easysearch.adapter.settings

import android.content.Context
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDivider
import com.google.android.material.switchmaterial.SwitchMaterial
import ru.tech.easysearch.R
import ru.tech.easysearch.data.SharedPreferencesAccess.HIDE_PANELS
import ru.tech.easysearch.data.SharedPreferencesAccess.SET
import ru.tech.easysearch.data.SharedPreferencesAccess.needToChangeBrowserSettings
import ru.tech.easysearch.data.SharedPreferencesAccess.setSetting
import ru.tech.easysearch.databinding.BrowserSettingsItemBinding
import ru.tech.easysearch.databinding.SettingHeaderItemBinding
import ru.tech.easysearch.model.SettingsItem


class BrowserSettingsAdapter(
    private var context: Context,
    private var settingsList: List<SettingsItem>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM -> ItemViewHolder(
                BrowserSettingsItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> HeaderViewHolder(
                SettingHeaderItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(mainHolder: RecyclerView.ViewHolder, position: Int) {
        val settingsItem = settingsList[position]
        when (settingsItem.key) {
            HEADER.toString() -> {
                val holder = mainHolder as HeaderViewHolder

                if (position == 0) holder.divider.visibility = GONE
                else holder.divider.visibility = VISIBLE

                holder.icon.setImageDrawable(settingsList[position].icon)
                holder.label.text = settingsItem.label
            }
            else -> {
                val holder = mainHolder as ItemViewHolder

                holder.icon.setImageDrawable(settingsItem.icon)
                holder.label.text = settingsItem.label
                holder.switcher.isChecked = settingsItem.checked
                holder.switcher.setOnCheckedChangeListener { _, isChecked ->
                    setSetting(context, settingsItem.key, isChecked)
                    if(settingsItem.key == HIDE_PANELS) Toast.makeText(context, R.string.restartBrowser, Toast.LENGTH_LONG).show()
                    needToChangeBrowserSettings(context, SET)
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return settingsList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (settingsList[position].key == HEADER.toString()) HEADER
        else ITEM
    }

    inner class ItemViewHolder(binding: BrowserSettingsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val icon: ImageView = binding.icon
        val label: TextView = binding.label
        val switcher: SwitchMaterial = binding.switcher
    }

    inner class HeaderViewHolder(binding: SettingHeaderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val label: TextView = binding.label
        val icon: ImageView = binding.icon
        val divider: MaterialDivider = binding.divider
    }

    companion object {
        const val HEADER = 0
        const val ITEM = 1
    }

}
