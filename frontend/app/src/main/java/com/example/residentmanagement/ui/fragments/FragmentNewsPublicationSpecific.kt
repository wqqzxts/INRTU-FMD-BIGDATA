package com.example.residentmanagement.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.residentmanagement.R
import com.example.residentmanagement.data.model.Publication
import com.example.residentmanagement.data.network.RetrofitClient
import com.example.residentmanagement.data.util.AuthManager
import com.example.residentmanagement.ui.activities.ActivityMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentNewsPublicationSpecific : Fragment() {
    private lateinit var publicationDate: TextView
    private lateinit var publicationTitle: TextView
    private lateinit var publicationContent: TextView
    private var publicationId: Int = -1
    private lateinit var authManager: AuthManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_news_publication_specific, container, false)

        publicationId = arguments?.getInt("PUBLICATION_ID", -1) ?: -1
        if (publicationId == -1 ) {
            Toast.makeText(requireContext(), "Публикация не была найдена!", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }

        publicationDate = view.findViewById(R.id.publication_date)
        publicationTitle = view.findViewById(R.id.publication_title)
        publicationContent = view.findViewById(R.id.publication_content)

        loadSpecificPublication(publicationId)

        return view
    }

    private fun loadSpecificPublication(publicationId: Int) {
        lifecycleScope.launch {

            val result = kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    RetrofitClient.getApiService().getSpecificPublication(publicationId)
                }
            }

            result.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let { publication ->
                        handleSuccessfulLoad(publication)
                    } ?: run {
                        Log.e("FragmentNewsPublicationSpecifi GET load specific publication", "Empty body in response")
                    }
                } else if (response.code() == 401) {
                    loadSpecificPublication(publicationId)
                } else if (response.code() == 403) {
                    handleFailedLoad()
                }
            }.onFailure { e ->
                Log.e("FragmentNewsPublicationEdit GET specific publication", "Error: ${e.message}")
            }
        }
    }

    private fun handleSuccessfulLoad(publication: Publication) {
        publicationDate.text = publication.datePublished.toString()
        publicationTitle.text = publication.title
        publicationContent.text = publication.content
    }

    private fun handleFailedLoad() {
        authManager.isSessionExpiredFromApp = true
        Toast.makeText(requireContext(), "Сессия истекла. Войдите снова", Toast.LENGTH_SHORT).show()
        val intent = Intent(requireContext(), ActivityMain::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        requireActivity().finish()
    }
}