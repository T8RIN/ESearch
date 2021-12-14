package ru.tech.easysearch.adapter.lables

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bekawestberg.loopinglayout.library.LoopingLayoutManager
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.tech.easysearch.R
import ru.tech.easysearch.activity.SearchResultsActivity
import ru.tech.easysearch.activity.MainActivity


class LabelListAdapter(
    private val context: Context,
    var labelList: List<Int>,
    private val card: MaterialCardView,
    private val fab: FloatingActionButton?,
    private val labelRecycler: RecyclerView,
    private val toolbarRecycler: RecyclerView,
    private val forward: ImageButton?,
    private val backward: ImageButton?,
    private val manageList: ImageButton,
    private val close: ImageButton
) :
    RecyclerView.Adapter<LabelListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.label_item, parent, false)
        )
    }

    private var backButton: ImageView? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.label.setImageResource(labelList[position])

        holder.card.isChecked =
            position == (toolbarRecycler.layoutManager as LoopingLayoutManager).findLastCompletelyVisibleItemPosition()

        holder.itemView.setOnClickListener {
            if (context is SearchResultsActivity) {
                backButton = context.findViewById(R.id.backButton)
            }
            card.animate()
                .y(MainActivity.displayOffsetY)
                .setDuration(350)
                .withEndAction {
                    labelRecycler.adapter = null
                    fab?.show()
                    card.visibility = View.GONE
                }
                .withStartAction {
                    forward?.animate()?.y(0f)?.setDuration(300)?.start()
                    backward?.animate()?.y(0f)?.setDuration(300)?.start()
                    toolbarRecycler.scrollToPosition(position)
                    close.animate().x(MainActivity.displayOffsetX).setDuration(200).start()
                    manageList.animate().y(MainActivity.displayOffsetY).setDuration(300).start()
                    backButton?.animate()?.y(0f)?.setDuration(200)?.start()
                }
                .start()
        }
    }

    override fun getItemCount(): Int {
        return labelList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val label: ImageView = view.findViewById(R.id.label)
        val card: MaterialCardView = view.findViewById(R.id.labelCard)
    }

}
