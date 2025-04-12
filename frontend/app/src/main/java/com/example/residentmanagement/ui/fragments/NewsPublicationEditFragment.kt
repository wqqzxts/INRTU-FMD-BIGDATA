package com.example.residentmanagement.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.residentmanagement.R
import com.example.residentmanagement.data.model.Publication
import com.example.residentmanagement.data.model.RequestCreateEditPublication
import com.example.residentmanagement.data.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewsPublicationEditFragment : Fragment() {
    private lateinit var titleInput: EditText
    private lateinit var contentInput: EditText
    private lateinit var editPublicationButton: Button
    private var publicationId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_publication, container, false)

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

    private fun editPublication() {
        val title = titleInput.text.toString()
        val content  = contentInput.text.toString()

        val request = RequestCreateEditPublication(title, content)

        RetrofitClient.getApiService().updateSpecificPublication(publicationId, request).enqueue(object : Callback<Publication> {
            override fun onResponse(call: Call<Publication>, response: Response<Publication>) {
                if (response.code() == 200) {
                    Toast.makeText(requireContext(), "Публикация была изменена успешно!", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
                if (response.code() == 403) {
                    Toast.makeText(requireContext(), "У вас нет прав", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
                if (response.code() == 400) {
                    val errorBody = response.errorBody()?.string()
                    Log.e("POST Publication", "Error: $errorBody")
                    Toast.makeText(requireContext(), "Некорректный запрос. Попробуйте еще раз.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Publication>, t: Throwable) {
                Log.e("PATCH publication news", "Error: ${t.message}")
            }
        })
    }

    private fun loadSpecificPublication(publicationId: Int) {
        RetrofitClient.getApiService().getSpecificPublication(publicationId).enqueue(object : Callback<Publication> {
            override fun onResponse(call: Call<Publication>, response: Response<Publication>) {
                if (response.code() == 200) {
                    response.body()?.let { publication ->
                        titleInput.setText(publication.title)
                        contentInput.setText(publication.content)
                    }
                }
                if (response.code() == 404) {
                    Toast.makeText(requireContext(), "Публикация не была найдена!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Publication>, t: Throwable) {
                Log.e("GET specific publication.", "Error: ${t.message}")
            }

        })
    }
}