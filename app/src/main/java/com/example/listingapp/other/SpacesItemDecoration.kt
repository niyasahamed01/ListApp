package com.example.listingapp.other

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpacesItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        with(outRect) {
            left = spacing
            right = spacing
            bottom = spacing

            // Add top margin only for the first row to avoid double spacing between items
            if (parent.getChildAdapterPosition(view) < 2) {
                top = spacing
            }
        }
    }
}