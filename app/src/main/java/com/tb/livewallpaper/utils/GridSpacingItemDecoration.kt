package com.tb.livewallpaper.utils

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridSpacingItemDecoration(private val ctx: Context) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val totalCount = parent.adapter?.itemCount ?: 0
        val hSpacing = dpToPx(12f)
        val vSpacing = dpToPx(20f)

        outRect.bottom  = vSpacing
        if (position / totalCount == 0) {
            outRect.right = hSpacing
        } else {
            outRect.left = hSpacing
        }
    }

    private fun dpToPx(dp:Float):Int{
       val den = ctx.resources.displayMetrics.density
        return (dp*den+0.5F).toInt()
    }
}