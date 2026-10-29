package com.example.pr2

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri

class OrderContentProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.example.pr2.provider"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/orders")

        private const val ORDERS = 1
        private const val ORDER_ID = 2

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "orders", ORDERS)
            addURI(AUTHORITY, "orders/#", ORDER_ID)
        }
    }

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(): Boolean {
        context?.let {
            dbHelper = DatabaseHelper(it)
            return true
        }
        return false
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = when (uriMatcher.match(uri)) {
            ORDERS -> db.query(DatabaseHelper.TABLE_ORDERS, projection, selection, selectionArgs, null, null, sortOrder ?: "${DatabaseHelper.COL_ORDER_ID} DESC")
            ORDER_ID -> {
                val id = uri.lastPathSegment
                db.query(DatabaseHelper.TABLE_ORDERS, projection, "${DatabaseHelper.COL_ORDER_ID}=?", arrayOf(id), null, null, sortOrder)
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        cursor.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val db = dbHelper.writableDatabase
        val id = db.insert(DatabaseHelper.TABLE_ORDERS, null, values)
        if (id > 0) {
            val itemUri = ContentUris.withAppendedId(CONTENT_URI, id)
            context?.contentResolver?.notifyChange(itemUri, null)
            return itemUri
        }
        return null
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        val db = dbHelper.writableDatabase
        val count = when (uriMatcher.match(uri)) {
            ORDERS -> db.update(DatabaseHelper.TABLE_ORDERS, values, selection, selectionArgs)
            ORDER_ID -> {
                val id = uri.lastPathSegment
                db.update(DatabaseHelper.TABLE_ORDERS, values, "${DatabaseHelper.COL_ORDER_ID}=?", arrayOf(id))
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        context?.contentResolver?.notifyChange(uri, null)
        return count
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val db = dbHelper.writableDatabase
        val count = when (uriMatcher.match(uri)) {
            ORDERS -> db.delete(DatabaseHelper.TABLE_ORDERS, selection, selectionArgs)
            ORDER_ID -> {
                val id = uri.lastPathSegment
                db.delete(DatabaseHelper.TABLE_ORDERS, "${DatabaseHelper.COL_ORDER_ID}=?", arrayOf(id))
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        context?.contentResolver?.notifyChange(uri, null)
        return count
    }

    override fun getType(uri: Uri): String {
        return when (uriMatcher.match(uri)) {
            ORDERS -> "vnd.android.cursor.dir/vnd.com.example.pr2.orders"
            ORDER_ID -> "vnd.android.cursor.item/vnd.com.example.pr2.orders"
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }
}