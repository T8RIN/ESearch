package ru.tech.easysearch.custom.popup

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDivider
import com.google.android.material.switchmaterial.SwitchMaterial
import ru.tech.easysearch.R
import ru.tech.easysearch.activity.BrowserActivity
import ru.tech.easysearch.helper.interfaces.DesktopInterface

class PopupMenuAdapter(
    private val context: Context,
    private val desktopInterface: DesktopInterface?
) :
    RecyclerView.Adapter<PopupMenuAdapter.PopupViewHolder>() {

    private var menuList: ArrayList<PopupMenuItem> = ArrayList()
    private var popupMenuItemClickListener: PopupMenuItemClickListener =
        PopupMenuItemClickListener {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopupViewHolder {
        return PopupViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.popup_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PopupViewHolder, position: Int) {
        val popupMenuItem = menuList[position]
        if (popupMenuItem.icon != null) {
            holder.bind(popupMenuItem)

            holder.image.setImageDrawable(popupMenuItem.icon)
            holder.text.text = popupMenuItem.title

            if (popupMenuItem.showSwitcher) {
                holder.itemView.isClickable = false
                holder.switcher.visibility = VISIBLE
                holder.switcher.isChecked =
                    (context as BrowserActivity).browser?.isDesktop() == true
                holder.switcher.setOnCheckedChangeListener { _, isChecked ->
                    desktopInterface?.changeUserAgent(isChecked)
                }
            } else holder.switcher.visibility = GONE

            if (popupMenuItem.showDivider) holder.divider.visibility = VISIBLE
            else holder.divider.visibility = GONE

        } else {
            holder.image.visibility = GONE
            holder.text.text = popupMenuItem.title
            holder.switcher.visibility = GONE
            holder.divider.visibility = GONE
        }
    }

    override fun getItemCount(): Int {
        return menuList.size
    }

    fun reattachListener(popupMenuItemClickListener: PopupMenuItemClickListener) {
        this.popupMenuItemClickListener = popupMenuItemClickListener
    }

    fun attachList(menuList: Array<out PopupMenuItem>) {
        for (i in menuList) this.menuList.add(i)
    }

    inner class PopupViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.image)
        val text: TextView = view.findViewById(R.id.text)
        val switcher: SwitchMaterial = view.findViewById(R.id.switcher)
        val divider: MaterialDivider = view.findViewById(R.id.divider)

        fun bind(popupMenuItem: PopupMenuItem) {
            itemView.setOnClickListener {
                popupMenuItemClickListener.onClick(popupMenuItem)
            }
        }
    }

}
