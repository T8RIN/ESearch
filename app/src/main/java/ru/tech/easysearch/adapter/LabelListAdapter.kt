package ru.tech.easysearch.adapter

import android.content.Context
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


class LabelListAdapter(
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
    RecyclerView.Adapter<LabelListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.label_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.label.setImageResource(labelList[position])
        holder.itemView.setOnClickListener {
            card.animate()
                .y(MainActivity.displayOffsetY)
                .setDuration(350)
                .withEndAction {
                    labelRecycler.adapter = null
                    fab?.show()
                    card.visibility = View.GONE
                }
                .withStartAction {
                    forward.animate().y(0f).setDuration(300).start()
                    backward.animate().y(0f).setDuration(300).start()
                    toolbarRecycler.scrollToPosition(position)
                    manageList.animate().x(MainActivity.displayOffsetX).setDuration(200).start()
                }
                .start()
        }
    }

    override fun getItemCount(): Int {
        return labelList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val label: ImageView = view.findViewById(R.id.label)
    }

}
