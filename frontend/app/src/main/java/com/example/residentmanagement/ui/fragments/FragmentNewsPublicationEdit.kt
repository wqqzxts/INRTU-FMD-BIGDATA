package com.example.residentmanagement.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

import com.example.residentmanagement.R
import com.example.residentmanagement.data.model.RequestCreateEditPublication
import com.example.residentmanagement.data.network.RetrofitClient
import com.example.residentmanagement.data.util.AuthManager
import com.example.residentmanagement.ui.activities.ActivityMain

class FragmentNewsPublicationEdit : Fragment() {
    private lateinit var titleInput: EditText
    private lateinit var contentInput: EditText
    private lateinit var editPublicationButton: Button
    private var publicationId: Int = -1
    private lateinit var authManager: AuthManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_news_publication_edit, container, false)

        publicationId = arguments?.getInt("PUBLICATION_ID", -1) ?: -1
        if (publicationId == -1 ) {
            Toast.makeText(requireContext(), "Публикация не была найдена!", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }

        titleInput = view.findViewById(R.id.edit_title_input)
        contentInput = view.findViewById(R.id.edit_content_input)
        editPublicationButton = view.findViewById(R.id.button_edit_publication_form)

        loadSpecificPublication(publicationId)

        editPublicationButton.setOnClickListener {
            editPublication()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authManager = AuthManager(requireContext())
    }

    private fun editPublication() {
        lifecycleScope.launch {
            try {
                val title = titleInput.text.toString()
                val content  = contentInput.text.toString()

                val request = RequestCreateEditPublication(title, content)

                val response = RetrofitClient.getApiService().updateSpecificPublication(publicationId, request)

                if (response.code() == 200) {
                    Toast.makeText(requireContext(), "Публикация была изменена успешно!", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
                if (response.code() == 401) {
                    editPublication()
                }
                if (response.code() == 403) {
                    authManager.isSessionExpiredFromApp = true
                    Toast.makeText(requireContext(), "Сессия истекла. Войдите снова", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireContext(), ActivityMain::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)
                    requireActivity().finish()
                }
                if (response.code() == 400) {
                    val errorBody = response.errorBody()?.string()
                    Log.e("FragmentNewsPublicationEdit PATCH publication", "Error: $errorBody")
                    Toast.makeText(requireContext(), "Некорректный запрос. Попробуйте еще раз.", Toast.LENGTH_SHORT).show()
                    return@launch
                }
            } catch (e: Exception) {
                Log.e("FragmentNewsPublicationEdit PATCH publication", "Error: ${e.message}")
            }
        }
    }

    private fun loadSpecificPublication(publicationId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApiService().getSpecificPublication(publicationId)

                if (response.code() == 200) {
                    val publication = response.body()
                    if (publication != null) {
                        titleInput.setText(publication.title)
                        contentInput.setText(publication.content)
                    } else {
                        Log.e("FragmentNewsPublicationEdit GET specific publication", "Empty body in response")
                    }
                }
                if (response.code() == 401) {
                    loadSpecificPublication(publicationId)
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
                Log.e("FragmentNewsPublicationEdit GET specific publication", "Error: ${e.message}")
            }
        }
    }
}