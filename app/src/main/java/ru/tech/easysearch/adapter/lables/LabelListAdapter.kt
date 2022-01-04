package ru.tech.easysearch.adapter.lables

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bekawestberg.loopinglayout.library.LoopingLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.tech.easysearch.activity.MainActivity
import ru.tech.easysearch.databinding.LabelItemBinding
import ru.tech.easysearch.extensions.Extensions.getResId

class LabelListAdapter(
    private val context: Context,
    var labelList: List<String>,
    private val card: MaterialCardView,
    private val fab: FloatingActionButton?,
    private val labelRecycler: RecyclerView,
    private val toolbarRecycler: RecyclerView,
    private val forward: ImageButton?,
    private val backward: ImageButton?,
    private val manageList: ImageButton,
    private val close: ImageButton,
    private var backButton: ImageView? = null
) :
    RecyclerView.Adapter<LabelListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LabelItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context).load(labelList[position].getResId()).into(holder.label)

        holder.card.isChecked =
            position == (toolbarRecycler.layoutManager as LoopingLayoutManager).findLastCompletelyVisibleItemPosition()

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

    inner class ViewHolder(binding: LabelItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val label: ImageView = binding.label
        val card: MaterialCardView = binding.root
    }

}
