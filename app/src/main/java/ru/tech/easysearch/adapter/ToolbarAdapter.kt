package ru.tech.easysearch.adapter

import android.content.Context
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.tech.easysearch.R
import ru.tech.easysearch.activity.MainActivity
import ru.tech.easysearch.activity.MainActivity.Companion.displayOffsetX
import ru.tech.easysearch.activity.MainActivity.Companion.displayOffsetY


class ToolbarAdapter(
    private val context: Context,
    var labelList: List<Int>,
    private val card: MaterialCardView,
    private val fab: FloatingActionButton?,
    private val labelRecycler: RecyclerView,
    private val toolbarRecycler: RecyclerView,
    private val forward: ImageButton,
    private val backward: ImageButton,
    private val manageList: ImageButton
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
            when (card.translationY) {
                displayOffsetY -> {
                    card.animate()
                        .y(0f)
                        .setDuration(350)
                        .withStartAction {
                            labelRecycler.adapter = LabelListAdapter(
                                context,
                                labelList,
                                card,
                                fab,
                                labelRecycler,
                                toolbarRecycler,
                                forward,
                                backward,
                                manageList
                            )
                            fab?.hide()
                            card.visibility = View.VISIBLE
                            manageList.animate().x(0f).setDuration(200).start()
                        }
                        .start()
                    forward.animate().y(displayOffsetY).setDuration(1000).start()
                    backward.animate().y(displayOffsetY).setDuration(1000).start()
                }
                else -> {
                    card.animate()
                        .y(displayOffsetY)
                        .setDuration(350)
                        .withEndAction {
                            labelRecycler.adapter = null
                            fab?.show()
                            card.visibility = View.GONE
                        }
                        .start()
                    manageList.animate().x(displayOffsetX).setDuration(200).start()
                    forward.animate().y(0f).setDuration(300).start()
                    backward.animate().y(0f).setDuration(300).start()
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
