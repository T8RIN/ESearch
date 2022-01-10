package ru.tech.easysearch.custom.popup.smart

import android.content.Context
import android.content.res.Configuration
import android.view.Gravity.BOTTOM
import android.view.Gravity.END
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import ru.tech.easysearch.R
import ru.tech.easysearch.extensions.Extensions.dipToPixels
import ru.tech.easysearch.helper.interfaces.DesktopInterface
import ru.tech.easysearch.helper.utils.anim.AnimUtils.slideViewHorizontally
import ru.tech.easysearch.helper.utils.anim.AnimUtils.slideViewVertically


class SmartPopupMenu(
    private var root: ViewGroup,
    private val context: Context,
    desktopInterface: DesktopInterface? = null
) {

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
    private var recyclerView: RecyclerView

    private var adapter: SmartPopupMenuAdapter = SmartPopupMenuAdapter(context, desktopInterface)

    init {
        displayOffsetX = when (context.resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> context.resources.displayMetrics.heightPixels.toFloat()
            else -> context.resources.displayMetrics.widthPixels.toFloat()
        }
        popupView = LayoutInflater.from(context).inflate(R.layout.popup_menu, root, false)
        popupDismissTint =
            LayoutInflater.from(context).inflate(R.layout.popup_dismiss_tint, root, false)
        cardView = popupView.findViewById(R.id.card)

        recyclerView = popupView.findViewById(R.id.popupRecycler)
        recyclerView.adapter = adapter
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

        if (root.getChildAt(0) is CoordinatorLayout) {
            (popupView.layoutParams as FrameLayout.LayoutParams).apply {
                gravity = BOTTOM or END
                bottomMargin = context.dipToPixels(56f).toInt()
            }
        }

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

    fun setMenuItemClickListener(onClick: (SmartPopupMenuItem) -> Unit): SmartPopupMenu {
        adapter.reattachListener(SmartPopupMenuItemClickListener { popupMenuItem ->
            onClick(popupMenuItem)
            adapter.reattachListener(SmartPopupMenuItemClickListener {})
        })
        return this
    }

    fun addItems(vararg smartPopupMenuItem: SmartPopupMenuItem): SmartPopupMenu {
        adapter.attachList(smartPopupMenuItem)
        return this
    }

    fun setAnimDuration(duration: Long): SmartPopupMenu {
        this.animationDuration = duration
        return this
    }

}