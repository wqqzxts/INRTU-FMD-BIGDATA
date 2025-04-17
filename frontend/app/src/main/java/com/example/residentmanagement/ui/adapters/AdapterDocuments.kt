package com.example.residentmanagement.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.residentmanagement.R
import com.example.residentmanagement.data.model.ItemDocuments

class AdapterDocuments(
    private var items: MutableList<ItemDocuments>
) : RecyclerView.Adapter<AdapterDocuments.DocumentsViewHolder>() {

    var onFolderClickListener: ((ItemDocuments) -> Unit)? = null
    var onDeleteClickListener: ((ItemDocuments) -> Unit)? = null
    var onRenameClickListener: ((ItemDocuments) -> Unit)? = null

    inner class DocumentsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.file_icon)
        val name: TextView = itemView.findViewById(R.id.file_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_documents, parent, false)
        return DocumentsViewHolder(view)
    }

    override fun onBindViewHolder(holder: DocumentsViewHolder, position: Int) {
        val item = items[position]

        holder.name.text = item.name
        holder.icon.setImageResource(
            if (item.name == "..") R.drawable.ic_folder_up
            else R.drawable.ic_folder
        )

        holder.itemView.setOnClickListener {
            if (item.isDirectory) {
                onFolderClickListener?.invoke(item)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun getItemAt(position: Int) = items[position]

    fun updateItems(newItems: List<ItemDocuments>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun onItemSwipedToDelete(position: Int) {
        if (position < items.size && items[position].canEdit) {
            onDeleteClickListener?.invoke(items[position])
        }
    }

    fun onItemSwipedToRename(position: Int) {
        if (position < items.size && items[position].canEdit) {
            onRenameClickListener?.invoke(items[position])
        }
    }
}