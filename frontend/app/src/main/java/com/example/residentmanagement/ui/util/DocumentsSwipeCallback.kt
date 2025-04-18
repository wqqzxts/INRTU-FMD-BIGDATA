package com.example.residentmanagement.ui.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.residentmanagement.R
import com.example.residentmanagement.ui.adapters.AdapterDocuments

class DocumentsSwipeCallback(
    private val adapter: AdapterDocuments,
    private val context: Context
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private val bgColorDelete = ContextCompat.getColor(context, R.color.red)
    private val bgColorRename = ContextCompat.getColor(context, R.color.blue)

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.bindingAdapterPosition
        when (direction) {
            ItemTouchHelper.LEFT -> adapter.onItemSwipedToDelete(position)
            ItemTouchHelper.RIGHT -> adapter.onItemSwipedToRename(position)
        }
    }

    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val item = adapter.getItemAt(viewHolder.bindingAdapterPosition)

        // Don't allow swipe for ".." item
        if (!item.canEdit) {
            super.onChildDraw(canvas, recyclerView, viewHolder, 0f, dY, actionState, isCurrentlyActive)
            return
        }

        if (dX > 0) {
            val background = ColorDrawable(bgColorRename)
            background.setBounds(
                itemView.left,
                itemView.top,
                itemView.left + dX.toInt(),
                itemView.bottom
            )
            background.draw(canvas)
        } else if (dX < 0) {
            val background = ColorDrawable(bgColorDelete)
            background.setBounds(
                itemView.right + dX.toInt(),
                itemView.top,
                itemView.right,
                itemView.bottom
            )
            background.draw(canvas)
        }

        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = 0.6f
    override fun getAnimationDuration(recyclerView: RecyclerView, animationType: Int, animateDx: Float, animateDy: Float): Long = 250
}