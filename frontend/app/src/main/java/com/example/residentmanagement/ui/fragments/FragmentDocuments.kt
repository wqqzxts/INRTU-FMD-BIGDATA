package com.example.residentmanagement.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.residentmanagement.R
import com.example.residentmanagement.data.model.ItemDocuments
import com.example.residentmanagement.ui.adapters.AdapterDocuments
import com.example.residentmanagement.ui.util.DocumentsSwipeCallback
import java.io.File


class FragmentDocuments : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var documentsAdapter: AdapterDocuments
    private lateinit var menuButton: ImageButton
    private lateinit var currentDirectory: File
    private val rootDirectory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "ResidentManagement")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_documents, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.documents_recycler_view)
        menuButton = view.findViewById(R.id.news_menu_button)

        // Create root directory if it doesn't exist
        if (!rootDirectory.exists()) {
            rootDirectory.mkdirs()
        }
        currentDirectory = rootDirectory

        menuButton.setOnClickListener { v ->
            showPopupMenu(v)
        }

        setupRecyclerView()
        loadDocuments()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        documentsAdapter = AdapterDocuments(mutableListOf()).apply {
            onFolderClickListener = { folder ->
                navigateToFolder(folder)
            }
            onDeleteClickListener = { folder ->
                deleteFolder(folder)
            }
            onRenameClickListener = { folder ->
                showRenameDialog(folder)
            }
        }
        recyclerView.adapter = documentsAdapter

        val itemTouchHelper = ItemTouchHelper(DocumentsSwipeCallback(documentsAdapter, requireContext()))
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun loadDocuments() {
        val items = mutableListOf<ItemDocuments>()

        // Add parent directory item if not in root
        if (currentDirectory != rootDirectory) {
            items.add(ItemDocuments(
                name = "..",
                isDirectory = true,
                path = currentDirectory.parent ?: rootDirectory.path,
                canEdit = false
            ))
        }

        // List all directories in current directory
        currentDirectory.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                items.add(ItemDocuments(
                    name = file.name,
                    isDirectory = true,
                    path = file.path,
                    canEdit = true
                ))
            }
        }

        documentsAdapter.updateItems(items)
    }

    private fun navigateToFolder(folder: ItemDocuments) {
        currentDirectory = File(folder.path)
        loadDocuments()
    }

    private fun showPopupMenu(v: View) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(R.menu.popup_menu_documents, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_create_folder -> {
                    showCreateFolderDialog()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showCreateFolderDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.create_folder_title))

        val input = EditText(requireContext())
        input.hint = getString(R.string.folder_name_hint)
        builder.setView(input)

        builder.setPositiveButton(getString(R.string.create)) { dialog, _ ->
            val folderName = input.text.toString().trim()
            if (folderName.isNotEmpty()) {
                createFolder(folderName)
            }
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun createFolder(folderName: String) {
        val newFolder = File(currentDirectory, folderName)
        if (newFolder.mkdir()) {
            loadDocuments()
        } else {
            Toast.makeText(requireContext(), getString(R.string.folder_creation_failed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteFolder(folder: ItemDocuments) {
        val folderToDelete = File(folder.path)
        if (folderToDelete.deleteRecursively()) {
            loadDocuments()
            Toast.makeText(requireContext(), getString(R.string.folder_deleted), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), getString(R.string.folder_deletion_failed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showRenameDialog(folder: ItemDocuments) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.rename_folder_title))

        val input = EditText(requireContext())
        input.setText(folder.name)
        builder.setView(input)

        builder.setPositiveButton(getString(R.string.rename)) { dialog, _ ->
            val newName = input.text.toString().trim()
            if (newName.isNotEmpty()) {
                renameFolder(folder, newName)
            }
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun renameFolder(folder: ItemDocuments, newName: String) {
        val oldFile = File(folder.path)
        val newFile = File(oldFile.parent, newName)

        if (oldFile.renameTo(newFile)) {
            loadDocuments()
            Toast.makeText(requireContext(), getString(R.string.folder_renamed), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), getString(R.string.folder_rename_failed), Toast.LENGTH_SHORT).show()
        }
    }
}