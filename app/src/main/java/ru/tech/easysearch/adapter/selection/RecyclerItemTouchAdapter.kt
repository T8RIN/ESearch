package ru.tech.easysearch.adapter.selection

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import ru.tech.easysearch.databinding.RecyclerViewItemBinding
import ru.tech.easysearch.helper.interfaces.LabelListChangedInterface
import java.util.*

class RecyclerItemTouchAdapter(
    context: Context,
    labelList: ArrayList<String>,
    private val disAdapter: DeSelectedLabelsAdapter,
    labelListChangedInterface: LabelListChangedInterface
) :
    RecyclerView.Adapter<RecyclerItemTouchAdapter.ViewHolder>() {

    private val itemTouchHelper by lazy {
        val simpleItemTouchCallback =
            object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val adapter = recyclerView.adapter as SelectedLabelsAdapter
                    val from = viewHolder.bindingAdapterPosition
                    val to = target.bindingAdapterPosition
                    adapter.notifyItemMoved(from, to)
                    Collections.swap(adapter.labelList, from, to)
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                }

                override fun onSelectedChanged(
                    viewHolder: RecyclerView.ViewHolder?,
                    actionState: Int
                ) {
                    super.onSelectedChanged(viewHolder, actionState)

                    if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                        viewHolder?.itemView?.alpha = 0.5f
                    }

                }

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    super.clearView(recyclerView, viewHolder)
                    viewHolder.itemView.alpha = 1.0f
                }
            }

        ItemTouchHelper(simpleItemTouchCallback)
    }

    var adapter: SelectedLabelsAdapter =
        SelectedLabelsAdapter(context, labelList, disAdapter, labelListChangedInterface)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        disAdapter.bindAdapter(adapter)
        return ViewHolder(
            RecyclerViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.recyclerView.adapter = adapter
        itemTouchHelper.attachToRecyclerView(holder.recyclerView)
    }

    override fun getItemCount(): Int {
        return 1
    }

    inner class ViewHolder(binding: RecyclerViewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val recyclerView: RecyclerView = binding.root
    }

}