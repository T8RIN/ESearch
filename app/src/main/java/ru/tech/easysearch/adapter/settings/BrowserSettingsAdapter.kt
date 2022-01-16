package ru.tech.easysearch.adapter.settings

import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.divider.MaterialDivider
import com.google.android.material.imageview.ShapeableImageView
import ru.tech.easysearch.R
import ru.tech.easysearch.data.DataArrays.colorList
import ru.tech.easysearch.data.DataArrays.colorListNames
import ru.tech.easysearch.data.SharedPreferencesAccess.EYE_PROTECTION
import ru.tech.easysearch.data.SharedPreferencesAccess.HIDE_PANELS
import ru.tech.easysearch.data.SharedPreferencesAccess.SET
import ru.tech.easysearch.data.SharedPreferencesAccess.getSetting
import ru.tech.easysearch.data.SharedPreferencesAccess.loadThemeVariant
import ru.tech.easysearch.data.SharedPreferencesAccess.needToChangeBrowserSettings
import ru.tech.easysearch.data.SharedPreferencesAccess.setSetting
import ru.tech.easysearch.databinding.BrowserSettingsItemBinding
import ru.tech.easysearch.databinding.SettingHeaderItemBinding
import ru.tech.easysearch.extensions.Extensions.dipToPixels
import ru.tech.easysearch.fragment.dialog.ColorPickerDialog
import ru.tech.easysearch.model.SettingsItem


class BrowserSettingsAdapter(
    private var context: Context,
    private var settingsList: List<List<SettingsItem>>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var parent: ViewGroup? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        this.parent = parent

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
        val settingsItems = settingsList[position]
        when (settingsItems[0].key) {
            HEADER.toString() -> {
                val holder = mainHolder as HeaderViewHolder

                if (position == 0) holder.divider.visibility = GONE
                else holder.divider.visibility = VISIBLE

                holder.icon.setImageDrawable(settingsList[position][0].icon)
                holder.label.text = settingsItems[0].label
            }
            else -> {
                val holder = mainHolder as ItemViewHolder

                for (item in settingsItems) {
                    val chip: Chip = LayoutInflater.from(context)
                        .inflate(R.layout.chip_item, parent, false) as Chip

                    chip.chipIcon = item.icon
                    chip.text = item.label
                    chip.isChecked = getSetting(context, item.key)
                    chip.id = Integer.parseInt(item.key)
                    var chipSize: Int

                    if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        val wid =
                            (context.resources.displayMetrics.widthPixels - context.dipToPixels(15f))

                        chipSize =
                            if (settingsItems.indexOf(item) == settingsItems.lastIndex && settingsItems.size % 2 != 0) MATCH_PARENT
                            else (wid / 2).toInt()

                        chip.layoutParams.apply { width = chipSize }

                    } else {
                        val ampl = (settingsItems.size + 1) * 5f
                        chip.layoutParams.apply {
                            width = ((context.resources.displayMetrics.widthPixels -
                                    context.dipToPixels(ampl)) / settingsItems.size).toInt()
                        }
                    }

                    val id = chip.id.toString()

                    chip.setOnCheckedChangeListener { _, isChecked ->
                        setSetting(context, id, isChecked)
                        if (id == HIDE_PANELS)
                            Toast.makeText(
                                context,
                                R.string.restartBrowser,
                                Toast.LENGTH_LONG
                            ).show()
                        needToChangeBrowserSettings(context, SET)
                    }

                    holder.chipGroup.addView(chip)
                }

                if (settingsItems[0].key == EYE_PROTECTION) {
                    holder.card.visibility = VISIBLE
                    holder.shapeImage.setStrokeColorResource(
                        colorList[colorListNames.indexOf(
                            loadThemeVariant(context)
                        )].first
                    )
                    holder.shapeImage.setImageResource(
                        colorList[colorListNames.indexOf(
                            loadThemeVariant(context)
                        )].second
                    )
                    holder.card.setOnClickListener {
                        val fragment = ColorPickerDialog()
                        if (!fragment.isAdded) fragment.show(
                            (context as AppCompatActivity).supportFragmentManager,
                            "pickColor"
                        )
                    }
                } else {
                    holder.card.visibility = GONE
                }
            }
        }

    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        (holder as? ItemViewHolder)?.chipGroup?.removeAllViews()
        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int {
        return settingsList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (settingsList[position].size == 1) HEADER
        else ITEM
    }

    inner class ItemViewHolder(binding: BrowserSettingsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val chipGroup: ChipGroup = binding.chipGroup
        val card: MaterialCardView = binding.card
        val shapeImage: ShapeableImageView = binding.shapeImage
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
