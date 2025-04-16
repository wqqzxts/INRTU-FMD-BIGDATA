package com.example.residentmanagement.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch

import com.example.residentmanagement.data.network.RetrofitClient
import com.example.residentmanagement.R
import com.example.residentmanagement.ui.adapters.AdapterPublications
import com.example.residentmanagement.data.model.Publication
import com.example.residentmanagement.data.util.AuthManager
import com.example.residentmanagement.ui.activities.MainActivity
import com.example.residentmanagement.ui.util.SwipeToEditDeleteCallback

class NewsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var publicationsAdapter: AdapterPublications
    private lateinit var publicationsList: MutableList<Publication>
    private lateinit var menuButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val authManager = AuthManager(requireContext())
        val isStaff = authManager.isStaff

        recyclerView = view.findViewById(R.id.news_recycler_view)
        menuButton = view.findViewById(R.id.news_menu_button)
        menuButton.visibility = if (isStaff) View.VISIBLE else View.GONE
        publicationsList = mutableListOf()

        menuButton.setOnClickListener {v ->
            showPopupMenu(v)
        }

        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        publicationsAdapter = AdapterPublications(publicationsList).apply {
            onDeleteClickListener = { publicationId ->
                if (isStaff) deletePublication(publicationId)
                else Toast.makeText(context, "Доступ запрещен", Toast.LENGTH_SHORT).show()
            }
            onEditClickListener = { publicationId ->
                if (isStaff) editPublication(publicationId)
                else Toast.makeText(context, "Доступ запрещен", Toast.LENGTH_SHORT).show()
            }
        }
        recyclerView.adapter = publicationsAdapter

        if (isStaff) {
            val itemTouchHelper = ItemTouchHelper(SwipeToEditDeleteCallback(publicationsAdapter, requireContext()))
            itemTouchHelper.attachToRecyclerView(recyclerView)
        }

        loadPublications()
    }

    private fun showPopupMenu(v: View) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(R.menu.popup_menu_news, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_create_publication -> {
                    val createFragment = NewsPublicationCreateFragment()
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.home_container, createFragment)
                        .addToBackStack("news_fragment")
                        .commit()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun loadPublications() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApiService().getPublications()

                if (response.code() == 200) {
                    val body = response.body()
                    if (body != null) {
                        val sortedPublications: List<Publication> = body.sortedByDescending { it.datePublished }

                        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                            override fun getOldListSize() = publicationsList.size
                            override fun getNewListSize() = sortedPublications.size
                            override fun areItemsTheSame(oldPos: Int, newPos: Int) =
                                publicationsList[oldPos].id == sortedPublications[newPos].id
                            override fun areContentsTheSame(oldPos: Int, newPos: Int) =
                                publicationsList[oldPos] == sortedPublications[newPos]
                        })

                        publicationsList.clear()
                        publicationsList.addAll(sortedPublications)
                        diffResult.dispatchUpdatesTo(publicationsAdapter)
                    } else {
                        Log.e("NewsFragment GET publications", "Empty body in response")
                    }
                }
                if (response.code() == 401) {
                    loadPublications()
                }
                if (response.code() == 403) {
                    Toast.makeText(requireContext(), "Сессия истекла. Войдите снова", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)
                    requireActivity().finish()
                }
            } catch (e: Exception) {
                Log.e("NewsFragment GET publications", "Error: ${e.message}")
            }
        }
    }

    private fun deletePublication(publicationId: Int) {
        lifecycleScope.launch {
            try {
                val response =
                    RetrofitClient.getApiService().deleteSpecificPublication(publicationId)

                if (response.code() == 200) {
                    val index = publicationsList.indexOfFirst { it.id == publicationId }
                    if (index != -1) {
                        publicationsList.removeAt(index)
                        publicationsAdapter.notifyItemRemoved(index)
                    }
                }
                if (response.code() == 401) {
                    loadPublications()
                }
                if (response.code() == 403) {
                    Toast.makeText(requireContext(), "Сессия истекла. Войдите снова", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)
                    requireActivity().finish()
                }
            } catch (e: Exception) {
                Log.e("NewsFragment DELETE publication", "Error: ${e.message}")
            }
        }
    }

    private fun editPublication(publicationId: Int) {
        val editFragment = NewsPublicationEditFragment().apply {
            arguments = Bundle().apply {
                putInt("PUBLICATION_ID", publicationId)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.home_container, editFragment)
            .addToBackStack("edit_publications")
            .commit()
    }
}