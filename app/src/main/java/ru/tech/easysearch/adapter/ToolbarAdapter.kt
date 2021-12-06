package ru.tech.easysearch.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.tech.easysearch.R


class ToolbarAdapter(
    val context: Context,
    var labelList: List<Int>,
    val card: MaterialCardView? = null,
    val fab: FloatingActionButton? = null
) :
    RecyclerView.Adapter<ToolbarAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.toolbar_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.label.setImageResource(labelList[position])
        holder.itemView.setOnClickListener {
            when (card?.translationY) {
                -2222f -> {
                    card.animate()
                        .y(0f)
                        .setDuration(200)
                        .withStartAction { fab?.hide() }
                        .start()
                }
                else -> {
                    card?.animate()
                        ?.y(-2222f)
                        ?.setDuration(200)
                        ?.withEndAction { fab?.show() }
                        ?.start()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return labelList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val label: ImageView = view.findViewById(R.id.label)
    }

}
