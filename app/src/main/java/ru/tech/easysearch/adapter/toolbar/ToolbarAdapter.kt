package ru.tech.easysearch.adapter.toolbar

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
import ru.tech.easysearch.activity.MainActivity.Companion.displayOffsetX
import ru.tech.easysearch.activity.MainActivity.Companion.displayOffsetY
import ru.tech.easysearch.activity.SearchResultsActivity
import ru.tech.easysearch.adapter.lables.LabelListAdapter
import ru.tech.easysearch.databinding.ToolbarItemBinding
import ru.tech.easysearch.extensions.Extensions.getResId


class ToolbarAdapter(
    private val context: Context,
    var labelList: List<String>,
    private val card: MaterialCardView,
    private val fab: FloatingActionButton?,
    private val labelRecycler: RecyclerView,
    private val toolbarRecycler: RecyclerView,
    private val forward: ImageButton?,
    private val backward: ImageButton?,
    private val manageList: ImageButton,
    private val close: ImageButton
) :
    RecyclerView.Adapter<ToolbarAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ToolbarItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    var labelListAdapter: LabelListAdapter? = null
    private var backButton: ImageView? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.label.setImageResource(R.drawable::class.java.getResId(labelList[position]))
        holder.itemView.setOnClickListener {
            if (context is SearchResultsActivity) {
                backButton = context.findViewById(R.id.backButton)
            }
            when (card.translationY) {
                displayOffsetY -> {
                    card.animate()
                        .y(0f)
                        .setDuration(350)
                        .withStartAction {
                            labelListAdapter = LabelListAdapter(
                                context,
                                labelList,
                                card,
                                fab,
                                labelRecycler,
                                toolbarRecycler,
                                forward,
                                backward,
                                manageList,
                                close
                            )
                            labelRecycler.adapter = labelListAdapter
                            fab?.hide()
                            card.visibility = View.VISIBLE
                            close.animate().x(0f).setDuration(200).start()
                            manageList.animate().y(0f).setDuration(200).start()
                            backButton?.animate()?.y(displayOffsetY)?.setDuration(200)?.start()
                        }
                        .start()
                    forward?.animate()?.y(displayOffsetY)?.setDuration(1000)?.start()
                    backward?.animate()?.y(displayOffsetY)?.setDuration(1000)?.start()
                }
                0f -> {
                    card.animate()
                        .y(displayOffsetY)
                        .setDuration(350)
                        .withEndAction {
                            labelListAdapter = null
                            labelRecycler.adapter = labelListAdapter
                            fab?.show()
                            card.visibility = View.GONE
                        }
                        .start()
                    close.animate().x(displayOffsetX).setDuration(200).start()
                    forward?.animate()?.y(0f)?.setDuration(300)?.start()
                    backward?.animate()?.y(0f)?.setDuration(300)?.start()
                    manageList.animate().y(displayOffsetY).setDuration(300).start()
                    backButton?.animate()?.y(0f)?.setDuration(200)?.start()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return labelList.size
    }

    inner class ViewHolder(binding: ToolbarItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val label: ImageView = binding.label
    }

}
