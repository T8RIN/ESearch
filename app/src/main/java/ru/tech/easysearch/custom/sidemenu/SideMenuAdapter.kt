package ru.tech.easysearch.custom.sidemenu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.tech.easysearch.R

class SideMenuAdapter : RecyclerView.Adapter<SideMenuAdapter.SideMenuViewHolder>() {

    private var menuList: ArrayList<SideMenuItem> = ArrayList()
    private var sideMenuItemClickListener: SideMenuItemClickListener = SideMenuItemClickListener {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SideMenuViewHolder {
        return SideMenuViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.side_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: SideMenuViewHolder, position: Int) {
        holder.image.setImageDrawable(menuList[position].icon)
        holder.text.text = menuList[position].title
        holder.bind(menuList[position])
    }

    override fun getItemCount(): Int {
        return menuList.size
    }

    fun reattachListener(sideMenuItemClickListener: SideMenuItemClickListener) {
        this.sideMenuItemClickListener = sideMenuItemClickListener
    }

    fun attachList(menuList: Array<out SideMenuItem>) {
        for (i in menuList) this.menuList.add(i)
    }

    inner class SideMenuViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.image)
        val text: TextView = view.findViewById(R.id.text)

        fun bind(sideMenuItem: SideMenuItem) {
            itemView.setOnClickListener {
                sideMenuItemClickListener.onClick(sideMenuItem)
            }
        }
    }

}
