package io.github.mylnzon58.velotv.ui.common

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Smooth TV-ergonomic LinearLayoutManager that keeps focused cards comfortably inside the
 * overscan-safe viewport with smooth GPU scrolling instead of abrupt edge snapping.
 */
class SmoothTvLayoutManager(
    context: Context,
    orientation: Int,
    private val paddingDp: Int
) : LinearLayoutManager(context, orientation, false) {

    private val padPx: Int = (paddingDp * context.resources.displayMetrics.density).toInt()

    override fun requestChildRectangleOnScreen(
        parent: RecyclerView,
        child: View,
        rect: Rect,
        immediate: Boolean,
        focusedChildVisible: Boolean
    ): Boolean {
        val parentLeft = parent.paddingLeft
        val parentTop = parent.paddingTop
        val parentRight = parent.width - parent.paddingRight
        val parentBottom = parent.height - parent.paddingBottom

        val childLeft = child.left + rect.left - child.scrollX
        val childTop = child.top + rect.top - child.scrollY
        val childRight = childLeft + rect.width()
        val childBottom = childTop + rect.height()

        var dx = 0
        var dy = 0

        if (canScrollHorizontally()) {
            val desiredLeft = parentLeft + padPx
            val desiredRight = parentRight - padPx
            dx = when {
                childLeft < desiredLeft -> childLeft - desiredLeft
                childRight > desiredRight -> childRight - desiredRight
                else -> 0
            }
        }

        if (canScrollVertically()) {
            val desiredTop = parentTop + padPx
            val desiredBottom = parentBottom - padPx
            dy = when {
                childTop < desiredTop -> childTop - desiredTop
                childBottom > desiredBottom -> childBottom - desiredBottom
                else -> 0
            }
        }

        if (dx != 0 || dy != 0) {
            if (immediate) {
                parent.scrollBy(dx, dy)
            } else {
                parent.smoothScrollBy(dx, dy)
            }
            return true
        }
        return false
    }
}

