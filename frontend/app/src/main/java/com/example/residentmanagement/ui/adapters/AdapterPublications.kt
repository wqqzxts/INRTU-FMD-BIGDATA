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
    private val publications: List<Publication>
) : RecyclerView.Adapter<AdapterPublications.PublicationViewHolder>() {
    var onDeleteClickListener: ((Int) -> Unit)? = null
    var onEditClickListener: ((Int) -> Unit)? = null

    class PublicationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val publicationDate: TextView = itemView.findViewById(R.id.publication_date)
        val publicationTitle: TextView = itemView.findViewById(R.id.publication_title)
        val publicationContent: TextView = itemView.findViewById(R.id.publication_content)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PublicationViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.recycler_row_news, parent, false)
        return PublicationViewHolder(view)
    }

    override fun getItemCount(): Int {
        return publications.size
    }

    override fun onBindViewHolder(holder: PublicationViewHolder, position: Int) {
        val publication: Publication = publications[position]
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val formattedDate = dateFormat.format(publication.datePublished)

        holder.publicationDate.text = formattedDate
        holder.publicationTitle.text = publication.title
        holder.publicationContent.text = publication.content

    }

    fun getPublicationAt(position: Int) = publications[position]

    fun onItemSwipedToDelete(publicationId: Int) {
        onDeleteClickListener?.invoke(publicationId)
    }

    fun onItemSwipedToEdit(publicationId: Int) {
        onEditClickListener?.invoke(publicationId)
    }
}