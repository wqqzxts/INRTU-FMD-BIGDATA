package com.example.residentmanagement.ui.fragments

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import java.io.File

import com.example.residentmanagement.R
import com.example.residentmanagement.data.model.ItemDocuments
import com.example.residentmanagement.ui.adapters.AdapterDocuments
import com.example.residentmanagement.ui.util.DocumentsSwipeCallback

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.FileOutputStream

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
        menuButton = view.findViewById(R.id.documents_menu_button)

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
        documentsAdapter = AdapterDocuments(mutableListOf())
        documentsAdapter.onItemClickListener = { item ->
            if (item.isDirectory) {
                navigateToDirectory(item)
            } else {
                openDocument(item)
            }
        }
        documentsAdapter.onDeleteClickListener = { item ->
            if (item.isDirectory) {
                deleteDirectory(item)
            } else {
                deleteDocument(item)
            }
        }
        documentsAdapter.onRenameClickListener = { item ->
            showRenameItemDialog(item)
        }
        recyclerView.adapter = documentsAdapter
        recyclerView.adapter = documentsAdapter

        val itemTouchHelper = ItemTouchHelper(DocumentsSwipeCallback(documentsAdapter, requireContext()))
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun loadDocuments() {
        val items = mutableListOf<ItemDocuments>()

        if (currentDirectory != rootDirectory) {
            items.add(ItemDocuments(
                name = "..",
                isDirectory = true,
                path = currentDirectory.parent ?: rootDirectory.path,
                canEdit = false
            ))
        }

        currentDirectory.listFiles()?.sortedBy { it.name }?.forEach { file ->
            items.add(ItemDocuments(
                name = file.name,
                isDirectory = file.isDirectory,
                path = file.path,
                canEdit = true,
                mimeType = if (!file.isDirectory) getMimeType(file) else null
            ))
        }

        documentsAdapter.updateItems(items)
    }

    private fun showPopupMenu(v: View) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(R.menu.popup_menu_documents, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_create_directory -> {
                    showCreateDirectoryDialog()
                    true
                }
                R.id.menu_create_pdf -> {
                    showCreateDocumentDialog("pdf", "application/pdf")
                    true
                }
                R.id.menu_create_word -> {
                    showCreateDocumentDialog("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                    true
                }
                R.id.menu_create_excel -> {
                    showCreateDocumentDialog("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun navigateToDirectory(folder: ItemDocuments) {
        currentDirectory = File(folder.path)
        loadDocuments()
    }

    private fun getMimeType(file: File): String {
        return when (file.extension.lowercase()) {
            "pdf" -> "application/pdf"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            else -> "*/*"
        }
    }

    private fun openDocument(item: ItemDocuments) {
        val file = File(item.path)
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, item.mimeType ?: "*/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                requireContext(),
                getString(R.string.toastFailureAppFind),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showCreateDirectoryDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.titleCreate))

        val input = EditText(requireContext())
        input.hint = getString(R.string.hintDirectoryName)
        builder.setView(input)

        builder.setPositiveButton(getString(R.string.buttonCreate)) { dialog, _ ->
            val folderName = input.text.toString().trim()
            if (folderName.isNotEmpty()) {
                createDirectory(folderName)
            }
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.buttonCancel)) { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun createDirectory(folderName: String) {
        val newFolder = File(currentDirectory, folderName)
        if (newFolder.mkdir()) {
            Toast.makeText(requireContext(), getString(R.string.toastSuccessDirectoryCreate), Toast.LENGTH_SHORT).show()
            loadDocuments()
        } else {
            Toast.makeText(requireContext(), getString(R.string.toastFailureDirectoryCreate), Toast.LENGTH_SHORT).show()
            loadDocuments()
        }
    }

    private fun deleteDirectory(folder: ItemDocuments) {
        val folderToDelete = File(folder.path)
        if (folderToDelete.deleteRecursively()) {
            loadDocuments()
            Toast.makeText(requireContext(), getString(R.string.toastSuccessDirectoryDelete), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), getString(R.string.toastFailureDirectoryDelete), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCreateDocumentDialog(extension: String, mimeType: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.titleCreate))

        val input = EditText(requireContext())
        input.hint = getString(R.string.hintDocumentName)
        builder.setView(input)

        builder.setPositiveButton(getString(R.string.buttonCreate)) { dialog, _ ->
            val fileName = input.text.toString().trim()
            if (fileName.isNotEmpty()) {
                createDocument(fileName, extension, mimeType)
            }
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.buttonCancel)) { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun createDocument(fileName: String, extension: String, mimeType: String) {
        val fullFileName = if (fileName.endsWith(".$extension")) fileName else "$fileName.$extension"
        val newFile = File(currentDirectory, fullFileName)

        try {
            when (extension) {
                "pdf" -> createPdfFile(newFile)
                "docx" -> createWordFile(newFile)
                "xlsx" -> createExcelFile(newFile)
                else -> newFile.createNewFile()
            }

            Toast.makeText(
                requireContext(),
                getString(R.string.toastSuccessDocumentCreate),
                Toast.LENGTH_SHORT
            ).show()
            loadDocuments()
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                getString(R.string.toastFailureDocumentCreate),
                Toast.LENGTH_SHORT
            ).show()
            e.printStackTrace()
        }
    }

    private fun createPdfFile(file: File) {
        val document = PDDocument()
        document.addPage(PDPage())
        document.save(file)
        document.close()
    }

    private fun createWordFile(file: File) {
        try {
            val document = XWPFDocument()
            document.createParagraph().createRun().setText("Resident Management Document")
            FileOutputStream(file).use { out ->
                document.write(out)
            }
            document.close()
        } catch (e: Exception) {
            throw e
        }
    }

    private fun createExcelFile(file: File) {
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Residents")
            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("First Name")
            headerRow.createCell(1).setCellValue("Last Name")
            headerRow.createCell(2).setCellValue("Gender")
            headerRow.createCell(3).setCellValue("Apartments")
            headerRow.createCell(4).setCellValue("E-mail")
            FileOutputStream(file).use { out ->
                workbook.write(out)
            }
            workbook.close()
        } catch (e: Exception) {
            throw e
        }

    }

    private fun deleteDocument(item: ItemDocuments) {
        val fileToDelete = File(item.path)
        if (fileToDelete.delete()) {
            loadDocuments()
            Toast.makeText(requireContext(), getString(R.string.toastSuccessDocumentDelete), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), getString(R.string.toastFailureDocumentDelete), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showRenameItemDialog(folder: ItemDocuments) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.titleRename))

        val input = EditText(requireContext())
        input.setText(folder.name)
        builder.setView(input)

        builder.setPositiveButton(getString(R.string.buttonRename)) { dialog, _ ->
            val newName = input.text.toString().trim()
            if (newName.isNotEmpty()) {
                renameItem(folder, newName)
            }
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.buttonCancel)) { dialog, _ ->
            dialog.cancel()
            loadDocuments()
        }

        builder.show()
    }

    private fun renameItem(item: ItemDocuments, newName: String) {
        val oldFile = File(item.path)
        val newFile = if (item.isDirectory) {
            File(oldFile.parent, newName)
        } else {
            val extension = oldFile.extension
            val newFileName = if (newName.endsWith(".$extension")) newName else "$newName.$extension"
            File(oldFile.parent, newFileName)
        }

        if (oldFile.renameTo(newFile)) {
            loadDocuments()
            Toast.makeText(requireContext(), getString(R.string.toastSuccessItemRename), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), getString(R.string.toastFailureItemRename), Toast.LENGTH_SHORT).show()
        }
    }
}