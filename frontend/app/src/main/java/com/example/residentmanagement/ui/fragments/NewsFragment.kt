package com.example.residentmanagement.ui.fragments

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
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.residentmanagement.data.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import com.example.residentmanagement.R
import com.example.residentmanagement.ui.adapters.AdapterPublications
import com.example.residentmanagement.data.model.Publication
import com.example.residentmanagement.data.util.AuthManager
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
        menuButton = view.findViewById(R.id.menu_button)
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
                else Toast.makeText(context, "Access denied", Toast.LENGTH_SHORT).show()
            }
            onEditClickListener = { publicationId ->
                if (isStaff) editPublication(publicationId)
                else Toast.makeText(context, "Access denied", Toast.LENGTH_SHORT).show()
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
        popup.menuInflater.inflate(R.menu.news_menu, popup.menu)

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
        val apiService = RetrofitClient.getApiService()

        apiService.getPublications().enqueue(object : Callback<List<Publication>> {
            override fun onResponse(call: Call<List<Publication>>, response: Response<List<Publication>>) {
                if (response.code() == 200) {
                    response.body()?.let { publications ->
                        publicationsList.clear()
                        publicationsList.addAll(publications)
                        publicationsAdapter.notifyDataSetChanged()
                    }
                }
            }

            override fun onFailure(call: Call<List<Publication>>, t: Throwable) {
                Log.e("Загрузка публикаций провалилась", "Ошибка сети: ${t.message}")
            }
        })
    }

    private fun deletePublication(publicationId: Int) {
        RetrofitClient.getApiService().deleteSpecificPublication(publicationId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.code() == 204) {
                    val index = publicationsList.indexOfFirst { it.id == publicationId }
                    publicationsList.removeAt(index)
                    publicationsAdapter.notifyItemRemoved(index)
                }
                if (response.code() == 403) {
                    Toast.makeText(requireContext(), "У вас нет прав", Toast.LENGTH_SHORT).show()
                    loadPublications()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("DELETE publication news", "Error: ${t.message}")
            }

        })
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