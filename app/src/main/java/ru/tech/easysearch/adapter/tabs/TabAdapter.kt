package ru.tech.easysearch.adapter.tabs

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.luminance
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import ru.tech.easysearch.R
import ru.tech.easysearch.activity.BrowserActivity
import ru.tech.easysearch.custom.view.BrowserView
import ru.tech.easysearch.data.BrowserTabItem
import ru.tech.easysearch.data.BrowserTabs.getCutSnap
import ru.tech.easysearch.data.BrowserTabs.loadTab
import ru.tech.easysearch.data.BrowserTabs.openedTabs
import ru.tech.easysearch.databinding.TabItemBinding
import ru.tech.easysearch.extensions.Extensions.darkenColor
import ru.tech.easysearch.extensions.Extensions.dipToPixels
import ru.tech.easysearch.extensions.Extensions.getAttrColor
import ru.tech.easysearch.extensions.Extensions.lightenColor
import ru.tech.easysearch.extensions.Extensions.setTint
import ru.tech.easysearch.fragment.tabs.TabsFragment
import ru.tech.easysearch.helper.utils.diff.TabDiffUtil
import java.net.URL


class TabAdapter(
    private val context: Context,
    private var adapterTabs: ArrayList<BrowserTabItem>,
    private val fragment: TabsFragment
) :
    RecyclerView.Adapter<TabAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            TabItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, wronPosition: Int) {
        val black = ContextCompat.getColor(
            context,
            R.color.black
        )
        val white = ContextCompat.getColor(
            context,
            R.color.white
        )
        var position = holder.layoutPosition

        (holder.itemView.layoutParams as RecyclerView.LayoutParams).apply {
            topMargin = if (position == 0) context.dipToPixels(6f).toInt()
            else context.dipToPixels(-14f).toInt()

            if (position == adapterTabs.lastIndex) {
                holder.shadow.visibility = GONE
            } else {
                holder.shadow.visibility = VISIBLE
            }
        }
        holder.snap.setImageBitmap(adapterTabs[position].fullSnap?.getCutSnap())
        holder.title.text = adapterTabs[position].title
        holder.url.text = URL(adapterTabs[position].url).host

        adapterTabs[position].fullSnap?.let { bitmap ->
            Palette.from(bitmap).generate { palette ->
                var vibrant = palette!!.getDominantColor(white)
                if (vibrant == white || vibrant == black) holder.snap.setImageResource(R.drawable.skeleton)

                if(vibrant.luminance < 0.1) vibrant = vibrant.lightenColor(0.25f)

                val titleColor: Int
                val urlColor: Int
                if (vibrant.luminance > 0.5) {
                    titleColor = black
                    urlColor = vibrant.darkenColor(0.4f)
                } else {
                    titleColor = white
                    urlColor = vibrant.lightenColor(0.4f)
                }

                holder.backgroundColor.setBackgroundColor(vibrant)

                holder.title.setTextColor(titleColor)
                holder.close.setTint(titleColor)
                holder.url.setTextColor(urlColor)
            }
        }

        holder.itemView.setOnClickListener {
            position = holder.layoutPosition
            if (context is BrowserActivity) {
                context.binding.webViewContainer.removeView(context.findViewById(R.id.webBrowser))
                context.binding.webViewContainer.removeAllViews()
                context.loadTab(position)
            } else {
                val intent = Intent(context, BrowserActivity::class.java)
                intent.putExtra("position", position)
                intent.putExtra("loadTab", true)
                context.startActivity(intent)
            }
            fragment.dismiss()
        }

        holder.close.setOnClickListener {
            val lastTab = adapterTabs.last().tab
            val removed = adapterTabs[holder.layoutPosition]

            openedTabs.remove(removed)
            Snackbar.make(fragment.requireView(), "${lastTab.url}", Snackbar.LENGTH_LONG)
                .setAction(R.string.undo) {
                    openedTabs.add(removed)
                    updateState()
                }
                .setBackgroundTint(
                    ContextCompat.getColor(
                        fragment.requireContext(),
                        R.color.materialGray
                    )
                )
                .setTextColor(ContextCompat.getColor(fragment.requireContext(), R.color.white))
                .setActionTextColor(context.getAttrColor(R.attr.colorSecondary))
                .setAnchorView(fragment.binding.addTab)
                .show()

            updateState()

            if (context is BrowserActivity) {
                context.apply {
                    if (findViewById<BrowserView>(R.id.webBrowser) === lastTab) {
                        fragment.notifyPosition(adapterTabs.lastIndex)
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateState() {
        val dfc = TabDiffUtil(adapterTabs, openedTabs)
        val difResult = DiffUtil.calculateDiff(dfc)
        adapterTabs.clear()
        adapterTabs.addAll(openedTabs)
        difResult.dispatchUpdatesTo(this)
        if (openedTabs.isNotEmpty()) {
            fragment.binding.label.text =
                "${context.getString(R.string.tabsOpened)} ${openedTabs.size}"
            fragment.binding.errorMessage.visibility = GONE
        } else {
            fragment.binding.label.text = context.getString(R.string.tabs)
            fragment.binding.errorMessage.visibility = VISIBLE
        }

    }

    override fun getItemCount(): Int {
        return adapterTabs.size
    }

    inner class ViewHolder(binding: TabItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val snap: ShapeableImageView = binding.snap
        val title: TextView = binding.title
        val url: TextView = binding.url
        val shadow: FrameLayout = binding.shadow
        val close: ImageButton = binding.close
        val backgroundColor: ShapeableImageView = binding.backgroundColor
    }

}
