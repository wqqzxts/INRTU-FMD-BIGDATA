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
import com.example.residentmanagement.data.model.RequestCreateEditPublication
import com.example.residentmanagement.data.network.RetrofitClient
import com.example.residentmanagement.ui.util.OnFragmentChangedListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewsPublicationCreateFragment : Fragment() {
    private lateinit var titleInput: EditText
    private lateinit var contentInput: EditText
    private lateinit var createPublicationButton: Button

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

    override fun onResume() {
        super.onResume()
        (activity as? OnFragmentChangedListener)?.onFragmentChanged(this)
    }

    private fun createPublication() {
        val title = titleInput.text.toString()
        val content = contentInput.text.toString()

        if (title.isEmpty() || content.isEmpty()){
            Toast.makeText(requireContext(), "Введите все поля формы!", Toast.LENGTH_SHORT).show()
        }

        val request = RequestCreateEditPublication(title, content)

        RetrofitClient.getApiService().createPublication(request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.code() == 200) {
                    Toast.makeText(requireContext(), "Публикация была создана успешно!", Toast.LENGTH_SHORT).show()
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

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("CREATE publication news", "Error: ${t.message}")
            }
        })
    }
}