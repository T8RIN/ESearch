package ru.tech.easysearch.custom

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import ru.tech.easysearch.R

class SideMenu(private var root: ViewGroup, context: Context) {

    var animationDuration = 1000L
    var openDirection = END
    var isCancelable = true
    var isHidden = true

    private var displayOffsetX = 1000F
    private var onDismiss: () -> Unit = {}

    private var sideView: View
    private var tintView: View
    private var cardView: MaterialCardView

    private var adapter: SideMenuAdapter = SideMenuAdapter()

    init {
        displayOffsetX = context.resources.displayMetrics.widthPixels.toFloat()
        sideView = LayoutInflater.from(context).inflate(R.layout.side_menu, root, false)
        tintView = LayoutInflater.from(context).inflate(R.layout.side_menu_tint, root, false)
        cardView = sideView.findViewById(R.id.card)
        sideView.translationX = displayOffsetX

        val layoutParams = cardView.layoutParams as FrameLayout.LayoutParams
        layoutParams.apply {
            height = WRAP_CONTENT
            width = (displayOffsetX * 0.66f).toInt()
            gravity = openDirection
        }
        val recycler: RecyclerView = sideView.findViewById(R.id.sideRecycler)
        recycler.adapter = adapter
        recycler.addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))
    }

    fun show() {
        root.addView(tintView)
        root.addView(sideView)

        tintView.animate().alpha(1f).setDuration(animationDuration)
            .withStartAction {
                if (isCancelable) {
                    tintView.setOnClickListener {
                        dismiss()
                    }
                }
                isHidden = false
            }
            .start()

        sideView.animate().x(0f).setDuration(animationDuration).start()
    }

    fun dismiss() {
        tintView.animate().alpha(0f).setDuration(animationDuration)
            .withStartAction {
                tintView.setOnClickListener {}
                isHidden = true
            }
            .withEndAction {
                root.removeView(tintView)
                root.removeView(sideView)
            }.start()

        when (openDirection) {
            START -> {
                sideView.animate().x(-displayOffsetX).setDuration(animationDuration)
                    .start()
            }
            END -> {
                sideView.animate().x(displayOffsetX).setDuration(animationDuration)
                    .start()
            }
        }

        onDismiss()
    }

    fun setOnDismissListener(onDismiss: () -> Unit): SideMenu {
        this.onDismiss = onDismiss
        return this
    }

    fun setCancelable(isCancelable: Boolean): SideMenu {
        this.isCancelable = isCancelable
        return this
    }

    fun setMenuItemClickListener(onClick: (SideMenuItem) -> Unit): SideMenu {
        adapter.reattachListener(SideMenuItemClickListener { sideMenuItem ->
            onClick(sideMenuItem)
        })
        return this
    }

    fun addItems(vararg sideMenuItem: SideMenuItem): SideMenu {
        adapter.attachList(sideMenuItem)
        return this
    }

    fun setAnimDuration(duration: Long): SideMenu {
        this.animationDuration = duration
        return this
    }

    fun setOpenDirection(direction: Int): SideMenu {
        openDirection = direction
        sideView.apply {
            translationX = when (openDirection) {
                START -> -displayOffsetX
                END -> displayOffsetX
                else -> throw IllegalArgumentException("No such gravity value")
            }
        }

        val layoutParams = cardView.layoutParams as FrameLayout.LayoutParams
        layoutParams.apply {
            gravity = openDirection
        }
        return this
    }

    companion object {
        const val START = Gravity.START
        const val END = Gravity.END
    }

}