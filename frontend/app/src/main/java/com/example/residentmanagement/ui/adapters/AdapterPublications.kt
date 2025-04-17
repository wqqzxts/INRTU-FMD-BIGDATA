package com.example.residentmanagement.ui.adapters

import android.view.LayoutInflater
import com.example.residentmanagement.data.model.Publication
import com.example.residentmanagement.R
import android.view.View
import android.view.ViewGroup
import java.text.SimpleDateFormat
import java.util.Locale
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdapterPublications(
    private val items: MutableList<Publication>
) : RecyclerView.Adapter<AdapterPublications.PublicationViewHolder>() {
    var onDeleteClickListener: ((Int) -> Unit)? = null
    var onEditClickListener: ((Int) -> Unit)? = null

    inner class PublicationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val publicationDate: TextView = itemView.findViewById(R.id.publication_date)
        val publicationTitle: TextView = itemView.findViewById(R.id.publication_title)
        val publicationContent: TextView = itemView.findViewById(R.id.publication_content)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PublicationViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_news, parent, false)
        return PublicationViewHolder(view)
    }

    override fun onBindViewHolder(holder: PublicationViewHolder, position: Int) {
        val item: Publication = items[position]
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val formattedDate = dateFormat.format(item.datePublished)

        holder.publicationDate.text = formattedDate
        holder.publicationTitle.text = item.title
        holder.publicationContent.text = item.content
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun getPublicationAt(position: Int) = items[position]

    fun onItemSwipedToDelete(publicationId: Int) {
        onDeleteClickListener?.invoke(publicationId)
    }

    fun onItemSwipedToEdit(publicationId: Int) {
        onEditClickListener?.invoke(publicationId)
    }
}