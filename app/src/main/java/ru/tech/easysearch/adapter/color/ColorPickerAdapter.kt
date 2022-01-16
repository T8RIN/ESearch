package ru.tech.easysearch.adapter.color

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import ru.tech.easysearch.data.DataArrays.colorListNames
import ru.tech.easysearch.data.DataArrays.colorNames
import ru.tech.easysearch.data.SharedPreferencesAccess
import ru.tech.easysearch.data.SharedPreferencesAccess.loadThemeVariant
import ru.tech.easysearch.databinding.ColorItemBinding
import ru.tech.easysearch.helper.interfaces.SettingsInterface

class ColorPickerAdapter(
    var context: Context,
    val fragment: DialogFragment,
    private var colorList: List<Pair<Int, Int>>,
    private val colorInterface: SettingsInterface?
) :
    RecyclerView.Adapter<ColorPickerAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ColorItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    private var selectedItemPos = -1
    private var lastItemSelectedPos = -1

    init {
        selectedItemPos = colorListNames.indexOf(loadThemeVariant(context))
        lastItemSelectedPos = selectedItemPos
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.image.setStrokeColorResource(colorList[position].first)
        holder.image.setImageResource(colorList[position].second)
        holder.text.text = colorNames[position]
        val amplifier = when (position) {
            0 -> SharedPreferencesAccess.RED
            1 -> SharedPreferencesAccess.PINK
            2 -> SharedPreferencesAccess.VIOLET
            3 -> SharedPreferencesAccess.BLUE
            4 -> SharedPreferencesAccess.MINT
            5 -> SharedPreferencesAccess.GREEN
            6 -> SharedPreferencesAccess.YELLOW
            else -> SharedPreferencesAccess.ORANGE
        }

        val card: MaterialCardView = holder.itemView as MaterialCardView

        card.isChecked = position == selectedItemPos

        holder.itemView.setOnClickListener {
            colorInterface?.onPickColor(amplifier)
            selectedItemPos = holder.absoluteAdapterPosition
            lastItemSelectedPos = if (lastItemSelectedPos == -1)
                selectedItemPos
            else {
                notifyItemChanged(lastItemSelectedPos)
                selectedItemPos
            }
            notifyItemChanged(selectedItemPos)
        }
    }

    override fun getItemCount(): Int {
        return colorList.size
    }

    inner class ViewHolder(binding: ColorItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val image: ShapeableImageView = binding.shapeImage
        val text: TextView = binding.text
    }
}