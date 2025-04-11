package com.example.residentmanagement.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.residentmanagement.R
import com.example.residentmanagement.ui.util.OnFragmentChangedListener

class DocumentsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_documents, container, false)
    }

    override fun onResume() {
        super.onResume()
        (activity as? OnFragmentChangedListener)?.onFragmentChanged(this)
    }
}