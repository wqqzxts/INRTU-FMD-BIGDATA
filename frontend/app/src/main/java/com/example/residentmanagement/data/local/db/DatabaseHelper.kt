package com.example.residentmanagement.data.local.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "resident_management.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_USER = "user"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_FIRST_NAME = "first_name"
        const val COLUMN_LAST_NAME = "last_name"

        const val TABLE_PUBLICATION = "publication"
        const val COLUMN_PUBLICATION_ID = "publication_id"
        const val COLUMN_DATE_PUBLISHED = "date_published"
        const val COLUMN_TITLE = "title"
        const val COLUMN_CONTENT = "content"
        const val COLUMN_USER_FK = "user_fk"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUserTable = """
            CREATE TABLE $TABLE_USER (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_FIRST_NAME TEXT NOT NULL,
                $COLUMN_LAST_NAME TEXT NOT NULL
            )
        """.trimIndent()

        val createPublicationTable = """
            CREATE TABLE $TABLE_PUBLICATION (       
                $COLUMN_PUBLICATION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_DATE_PUBLISHED TEXT NOT NULL,
                $COLUMN_TITLE,
                $COLUMN_CONTENT,
                $COLUMN_USER_FK INTEGER NOT NULL, FOREIGN KEY ($COLUMN_USER_FK) REFERENCES $TABLE_USER($COLUMN_USER_ID)
            )
        """.trimIndent()

        db.execSQL(createUserTable)
        db.execSQL(createPublicationTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PUBLICATION")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        onCreate(db)
    }

    fun dateToString(date: Date): String {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(date)
    }

    fun stringToDate(dateString: String): Date {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(dateString) ?: Date()
    }
}