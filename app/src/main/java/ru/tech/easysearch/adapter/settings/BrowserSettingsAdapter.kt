package ru.tech.easysearch.adapter.settings

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import ru.tech.easysearch.data.SharedPreferencesAccess.SET
import ru.tech.easysearch.data.SharedPreferencesAccess.needToChangeBrowserSettings
import ru.tech.easysearch.data.SharedPreferencesAccess.setSetting
import ru.tech.easysearch.databinding.BrowserSettingsItemBinding
import ru.tech.easysearch.model.SettingsItem


class BrowserSettingsAdapter(
    private var context: Context,
    private var settingsList: List<SettingsItem>
) :
    RecyclerView.Adapter<BrowserSettingsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            BrowserSettingsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val settingsItem = settingsList[position]
        holder.icon.setImageDrawable(settingsItem.icon)
        holder.label.text = settingsItem.label
        holder.switcher.isChecked = settingsItem.checked
        holder.switcher.setOnCheckedChangeListener { _, isChecked ->
            setSetting(context, settingsItem.key, isChecked)
            needToChangeBrowserSettings(context, SET)
        }
    }

    override fun getItemCount(): Int {
        return settingsList.size
    }

    inner class ViewHolder(binding: BrowserSettingsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val icon: ImageView = binding.icon
        val label: TextView = binding.label
        val switcher: SwitchMaterial = binding.switcher
    }

}
