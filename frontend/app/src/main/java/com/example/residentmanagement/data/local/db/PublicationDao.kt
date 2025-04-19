package com.example.residentmanagement.data.local.db

import android.content.ContentValues
import android.content.Context
import com.example.residentmanagement.data.model.Publication
import com.example.residentmanagement.data.model.User

class PublicationDao(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    private fun insertUser(user: User): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_FIRST_NAME, user.firstName)
            put(DatabaseHelper.COLUMN_LAST_NAME, user.lastName)
        }

        return db.insert(
            DatabaseHelper.TABLE_USER,
            null,
            values).also {
            db.close()
        }
    }

    fun insertPublication(publication: Publication): Long {
        val db = dbHelper.writableDatabase
        val userId = insertUser(publication.user)
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_PUBLICATION_ID, publication.id)
            put(DatabaseHelper.COLUMN_DATE_PUBLISHED, dbHelper.dateToString(publication.datePublished))
            put(DatabaseHelper.COLUMN_TITLE, publication.title)
            put(DatabaseHelper.COLUMN_CONTENT, publication.content)

            put(DatabaseHelper.COLUMN_USER_FK, userId)
        }

        return db.insert(DatabaseHelper.TABLE_PUBLICATION, null, values)
    }

    fun getPublications(): List<Publication> {
        val db = dbHelper.readableDatabase
        val publications = mutableListOf<Publication>()

        val query = """
            SELECT p.*, u.* 
            FROM ${DatabaseHelper.TABLE_PUBLICATION} p
            JOIN ${DatabaseHelper.TABLE_USER} u ON p.${DatabaseHelper.COLUMN_USER_FK} = u.${DatabaseHelper.COLUMN_USER_ID}
            ORDER BY p.${DatabaseHelper.COLUMN_DATE_PUBLISHED} DESC
        """.trimIndent()
        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()) {
            val publication = Publication(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PUBLICATION_ID)),
                title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE)),
                content = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CONTENT)),
                datePublished = dbHelper.stringToDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE_PUBLISHED))),
                user = User(
                    firstName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FIRST_NAME)),
                    lastName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAST_NAME))
                )
            )
            publications.add(publication)
        }

        cursor.close()
        return publications
    }

    fun updatePublication(publication: Publication): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_DATE_PUBLISHED, dbHelper.dateToString(publication.datePublished))
            put(DatabaseHelper.COLUMN_TITLE, publication.title)
            put(DatabaseHelper.COLUMN_CONTENT, publication.content)
        }

        return db.update(
            DatabaseHelper.TABLE_PUBLICATION,
            values,
            "${DatabaseHelper.COLUMN_PUBLICATION_ID} = ?",
            arrayOf(publication.id.toString())
        )
    }

    fun deletePublication(publicationId: Int): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            DatabaseHelper.TABLE_PUBLICATION,
            "${DatabaseHelper.COLUMN_PUBLICATION_ID} = ?",
            arrayOf(publicationId.toString())
        )
    }

    fun clearAllData() {
        val db = dbHelper.writableDatabase
        db.delete(DatabaseHelper.TABLE_PUBLICATION, null, null)
        db.delete(DatabaseHelper.TABLE_USER, null, null)
    }
}