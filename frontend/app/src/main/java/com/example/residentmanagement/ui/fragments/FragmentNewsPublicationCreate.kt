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
import com.example.residentmanagement.R
import com.example.residentmanagement.data.model.RequestCreateEditPublication
import com.example.residentmanagement.data.network.RetrofitClient
import com.example.residentmanagement.data.util.AuthManager
import com.example.residentmanagement.ui.activities.ActivityMain
import kotlinx.coroutines.launch

class FragmentNewsPublicationCreate : Fragment() {
    private lateinit var titleInput: EditText
    private lateinit var contentInput: EditText
    private lateinit var createPublicationButton: Button
    private lateinit var authManager: AuthManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_publication, container, false)

        titleInput = view.findViewById(R.id.create_title_input)
        contentInput = view.findViewById(R.id.create_content_input)
        createPublicationButton = view.findViewById(R.id.button_create_publication_form)

        createPublicationButton.setOnClickListener {
            createPublication()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authManager = AuthManager(requireContext())
    }

    private fun createPublication() {
        lifecycleScope.launch {
            try {
                val title = titleInput.text.toString()
                val content = contentInput.text.toString()

                if (title.isEmpty() || content.isEmpty()) {
                    Toast.makeText(requireContext(), "Введите все поля формы!", Toast.LENGTH_SHORT)
                        .show()
                    return@launch
                }

                val request = RequestCreateEditPublication(title, content)
                val response = RetrofitClient.getApiService().createPublication(request)

                if (response.code() == 200) {
                    Toast.makeText(
                        requireContext(),
                        "Публикация была создана успешно!",
                        Toast.LENGTH_SHORT
                    ).show()
                    parentFragmentManager.popBackStack()
                }
                if (response.code() == 401) {
                    createPublication()
                }
                if (response.code() == 403) {
                    authManager.isSessionExpiredFromApp = true
                    Toast.makeText(
                        requireContext(),
                        "Сессия истекла. Войдите снова",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(requireContext(), ActivityMain::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)
                    requireActivity().finish()
                }
                if (response.code() == 400) {
                    val errorBody = response.errorBody()?.string()
                    Log.e("FragmentNewsPublicationCreate POST publication", "Error: $errorBody")
                    Toast.makeText(requireContext(), "Некорректный запрос. Попробуйте еще раз.", Toast.LENGTH_SHORT).show()
                    return@launch
                }
            } catch (e: Exception) {
                Log.e("FragmentNewsPublicationCreate POST publication", "Error: ${e.message}")
            }
        }
    }
}