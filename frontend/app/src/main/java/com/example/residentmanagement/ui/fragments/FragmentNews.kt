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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.launch

import com.example.residentmanagement.data.network.RetrofitClient
import com.example.residentmanagement.R
import com.example.residentmanagement.data.local.db.PublicationDao
import com.example.residentmanagement.ui.adapters.AdapterPublications
import com.example.residentmanagement.data.model.Publication
import com.example.residentmanagement.data.util.AuthManager
import com.example.residentmanagement.ui.activities.ActivityMain
import com.example.residentmanagement.ui.util.NewsSwipeCallback

class FragmentNews : Fragment() {
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var publicationsAdapter: AdapterPublications
    private lateinit var publicationsList: MutableList<Publication>
    private lateinit var menuButton: ImageButton
    private lateinit var authManager: AuthManager
    private var isStaff: Boolean? = false
    private lateinit var publicationDao: PublicationDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_news, container, false)
        swipeRefreshLayout = view.findViewById(R.id.newsSwipeRefreshLayout)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authManager = AuthManager(requireContext())
        publicationDao = PublicationDao(requireContext())
        isStaff = authManager.isStaff

        recyclerView = view.findViewById(R.id.news_recycler_view)
        menuButton = view.findViewById(R.id.news_menu_button)
        menuButton.visibility = if (isStaff == true) View.VISIBLE else View.GONE
        publicationsList = mutableListOf()

        swipeRefreshLayout.setOnRefreshListener {
            loadPublications()
        }

        menuButton.setOnClickListener {v ->
            showPopupMenu(v)
        }

        setupRecyclerView()
        if (publicationDao.getPublications().isNotEmpty()) {
            val sortedPublications = publicationDao.getPublications()
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
            Toast.makeText(requireContext(), "Загружены локальные публикации", Toast.LENGTH_SHORT).show()
        } else {
            loadPublications()
        }
    }

    private fun setupRecyclerView() {

        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        publicationsAdapter = AdapterPublications(publicationsList)
        publicationsAdapter.onDeleteClickListener = { publicationId ->
            if (isStaff == true) deletePublication(publicationId)
            else Toast.makeText(context, "Доступ запрещен", Toast.LENGTH_SHORT).show()
        }
        publicationsAdapter.onEditClickListener = { publicationId ->
            if (isStaff == true) editPublication(publicationId)
            else Toast.makeText(context, "Доступ запрещен", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = publicationsAdapter

        if (isStaff == true) {
            val itemTouchHelper = ItemTouchHelper(NewsSwipeCallback(publicationsAdapter, requireContext()))
            itemTouchHelper.attachToRecyclerView(recyclerView)
        }
    }

    private fun loadPublications() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApiService().getPublications()

                if (response.code() == 200) {
                    val body = response.body()
                    if (body != null) {
                        body.forEach { publication ->
                            publicationDao.insertPublication(publication)
                        }

                        val sortedPublications: MutableList<Publication> = body.sortedByDescending { it.datePublished }.toMutableList()

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
                        Log.e("FragmentNews GET publications", "Empty body in response")
                    }
                }
                if (response.code() == 401) {
                    loadPublications()
                }
                if (response.code() == 403) {
                    authManager.isSessionExpiredFromApp = true
                    Toast.makeText(requireContext(), "Сессия истекла. Войдите снова", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireContext(), ActivityMain::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)
                    requireActivity().finish()
                }
            } catch (e: Exception) {
                Log.e("FragmentNews GET publications", "Error: ${e.message}")
            } finally {
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun showPopupMenu(v: View) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(R.menu.popup_menu_news, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_create_publication -> {
                    val createFragment = FragmentNewsPublicationCreate()
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

    private fun deletePublication(publicationId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApiService().deleteSpecificPublication(publicationId)

                if (response.code() == 204) {
                    publicationDao.deletePublication(publicationId)

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
                    authManager.isSessionExpiredFromApp = true
                    Toast.makeText(requireContext(), "Сессия истекла. Войдите снова", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireContext(), ActivityMain::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)
                    requireActivity().finish()
                }
            } catch (e: Exception) {
                Log.e("FragmentNews DELETE publication", "Error: ${e.message}")
            }
        }
    }

    private fun editPublication(publicationId: Int) {
        val editFragment = FragmentNewsPublicationEdit()
        val bundle = Bundle()
        bundle.putInt("PUBLICATION_ID", publicationId)
        editFragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.home_container, editFragment)
            .addToBackStack("edit_publications")
            .commit()
    }
}