package ru.tech.easysearch.custom.popup

import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import ru.tech.easysearch.R
import ru.tech.easysearch.helper.anim.AnimUtils.slideViewHorizontally
import ru.tech.easysearch.helper.anim.AnimUtils.slideViewVertically


class SmartPopupMenu(private var root: ViewGroup, context: Context) {

    private var animationDuration = 500L
    private var isCancelable = true
    var isHidden = true
    private var fullHeight = WRAP_CONTENT
    private var fullWidth = WRAP_CONTENT

    private var displayOffsetX = 1000F
    private var onDismiss: () -> Unit = {}

    private var popupView: View
    private var popupDismissTint: View
    private var cardView: MaterialCardView

    private var adapter: PopupMenuAdapter = PopupMenuAdapter(context)

    init {
        displayOffsetX = when (context.resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> context.resources.displayMetrics.heightPixels.toFloat()
            else -> context.resources.displayMetrics.widthPixels.toFloat()
        }
        popupView = LayoutInflater.from(context).inflate(R.layout.popup_menu, root, false)
        popupDismissTint =
            LayoutInflater.from(context).inflate(R.layout.popup_dismiss_tint, root, false)
        cardView = popupView.findViewById(R.id.card)

        val recycler: RecyclerView = popupView.findViewById(R.id.popupRecycler)
        recycler.adapter = adapter
    }

    fun show() {
        root.addView(popupDismissTint)
        root.addView(popupView)

        val wrapSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        cardView.measure(wrapSpec, wrapSpec)
        fullHeight = cardView.measuredHeight
        fullWidth = (displayOffsetX * 0.66f).toInt()

        if (isCancelable) {
            popupDismissTint.setOnClickListener {
                dismiss()
            }
        }
        isHidden = false

        cardView.slideViewVertically(
            0,
            fullHeight, startAction = {
                cardView.slideViewHorizontally(0, fullWidth, {}, {})
            }, { cardView.layoutParams.height = WRAP_CONTENT }
        )
    }

    fun dismiss() {
        cardView.slideViewVertically(
            fullHeight,
            0, startAction = {
                cardView.layoutParams.height = WRAP_CONTENT
                cardView.slideViewHorizontally(fullWidth, 0, {}, {})
                popupDismissTint.setOnClickListener(null)
                popupDismissTint.isClickable = false
                isHidden = true
            }, endAction = {
                root.removeView(popupDismissTint)
                root.removeView(popupView)
            }
        )

        onDismiss()
    }

    fun setOnDismissListener(onDismiss: () -> Unit): SmartPopupMenu {
        this.onDismiss = onDismiss
        return this
    }

    fun setCancelable(isCancelable: Boolean): SmartPopupMenu {
        this.isCancelable = isCancelable
        return this
    }

    fun setMenuItemClickListener(onClick: (PopupMenuItem) -> Unit): SmartPopupMenu {
        adapter.reattachListener(PopupMenuItemClickListener { popupMenuItem ->
            onClick(popupMenuItem)
            adapter.reattachListener(PopupMenuItemClickListener {})
        })
        return this
    }

    fun addItems(vararg popupMenuItem: PopupMenuItem): SmartPopupMenu {
        adapter.attachList(popupMenuItem)
        return this
    }

    fun setAnimDuration(duration: Long): SmartPopupMenu {
        this.animationDuration = duration
        return this
    }

}