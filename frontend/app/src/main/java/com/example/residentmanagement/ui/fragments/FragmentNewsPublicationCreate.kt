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
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.residentmanagement.R
import com.example.residentmanagement.data.model.RequestCreateEditPublication
import com.example.residentmanagement.data.network.RetrofitClient
import com.example.residentmanagement.data.util.AuthManager
import com.example.residentmanagement.ui.activities.ActivityMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        val view = inflater.inflate(R.layout.fragment_news_publication_create, container, false)

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
        val title = titleInput.text.toString()
        val content = contentInput.text.toString()
        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(requireContext(), "Введите все поля формы!", Toast.LENGTH_SHORT)
                .show()
            return
        }
        val request = RequestCreateEditPublication(title, content)

        lifecycleScope.launch {
            val result = kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    RetrofitClient.getApiService().createPublication(request)
                }
            }

            result.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let { publication ->
                        val publicationId = publication.id
                        handleSuccessfulCreation(publicationId)
                    } ?: run {
                        Log.e("FragmentNewsPublicationCreate POST create publication", "Empty body in response")
                    }
                } else if (response.code() == 401) {
                    createPublication()
                } else if (response.code() == 403) {
                    handleFailedCreation()
                }
            }.onFailure { e ->
                Log.e("FragmentNewsPublicationCreate POST publication", "Error: ${e.message}")
            }
        }
    }

    private fun handleFailedCreation() {
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

    private fun handleSuccessfulCreation(id: Int) {
        userWantSeePublication { wantsToSee ->
            if (wantsToSee) {
                val specificPublicationFragment = FragmentNewsPublicationSpecific()
                val bundle = Bundle()
                bundle.putInt("PUBLICATION_ID", id)
                specificPublicationFragment.arguments = bundle

                parentFragmentManager.beginTransaction()
                    .replace(R.id.home_container, specificPublicationFragment)
                    .addToBackStack("specific publication")
                    .commit()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Публикация была создана успешно!",
                    Toast.LENGTH_SHORT
                ).show()
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun userWantSeePublication(callback: (Boolean) -> Unit) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Посмотреть публикацию?")
        builder.setPositiveButton("Да") { dialog, _ ->
            dialog.dismiss()
            callback(true)
        }
        builder.setNegativeButton("Нет") { dialog, _ ->
            dialog.cancel()
            callback(false)
        }

        builder.show()
    }
}