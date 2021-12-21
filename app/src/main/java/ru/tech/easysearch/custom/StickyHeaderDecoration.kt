package ru.tech.easysearch.custom

import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import ru.tech.easysearch.R
import ru.tech.easysearch.helper.interfaces.StickyHeaderInterface


class StickyHeaderDecoration(private val stickyHeaderInterface: StickyHeaderInterface) :
    ItemDecoration() {

    private var mStickyHeaderHeight = 0

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)

        val topChildPosition = parent.getChildAdapterPosition(
            parent.getChildAt(0) ?: return
        )
        if (topChildPosition == RecyclerView.NO_POSITION) return

        val headerPos = stickyHeaderInterface.getHeaderPositionForItem(topChildPosition)
        val currentHeader = getHeaderViewForItem(headerPos, parent)
        fixLayoutSize(parent, currentHeader)
        val contactPoint = currentHeader.bottom
        val childInContact = getChildInContact(parent, contactPoint, headerPos)
        childInContact?.let {
            if (stickyHeaderInterface.isHeader(parent.getChildAdapterPosition(childInContact))) {
                moveHeader(c, currentHeader, childInContact)
                return
            }
        }
        drawHeader(c, currentHeader)
    }

    private fun getHeaderViewForItem(headerPosition: Int, parent: RecyclerView): View {
        val header =
            LayoutInflater.from(parent.context).inflate(R.layout.header_layout, parent, false)
        stickyHeaderInterface.bindHeaderData(header, headerPosition)
        return header
    }

    private fun drawHeader(c: Canvas, header: View) {
        c.apply {
            save()
            translate(0f, 0f)
            header.draw(this)
            restore()
        }
    }

    private fun moveHeader(c: Canvas, currentHeader: View, nextHeader: View) {
        c.apply {
            save()
            translate(0f, (nextHeader.top - currentHeader.height).toFloat())
            currentHeader.draw(this)
            restore()
        }
    }

    private fun getChildInContact(
        parent: RecyclerView,
        contactPoint: Int,
        currentHeaderPos: Int
    ): View? {
        var childInContact: View? = null
        for (i in 0 until parent.childCount) {
            var heightTolerance = 0
            val child = parent.getChildAt(i)

            if (currentHeaderPos != i && stickyHeaderInterface.isHeader(
                    parent.getChildAdapterPosition(
                        child
                    )
                )
            )
                heightTolerance = mStickyHeaderHeight - child.height

            val childBottomPosition: Int = if (child.top > 0) {
                child.bottom + heightTolerance
            } else {
                child.bottom
            }
            if (childBottomPosition > contactPoint) {
                if (child.top <= contactPoint) {
                    childInContact = child
                    break
                }
            }
        }
        return childInContact
    }

    private fun fixLayoutSize(parent: ViewGroup, view: View) {
        val childWidthSpec = ViewGroup.getChildMeasureSpec(
            View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY),
            parent.paddingLeft + parent.paddingRight,
            view.layoutParams.width
        )
        val childHeightSpec = ViewGroup.getChildMeasureSpec(
            View.MeasureSpec.makeMeasureSpec(parent.height, View.MeasureSpec.UNSPECIFIED),
            parent.paddingTop + parent.paddingBottom,
            view.layoutParams.height
        )
        view.apply {
            measure(childWidthSpec, childHeightSpec)
            layout(0, 0, measuredWidth, measuredHeight.also { mStickyHeaderHeight = it })
        }
    }

    companion object {
        const val HEADER = 0
        const val ITEM = 1
    }

}