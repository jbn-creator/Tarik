package com.example.tarik.data.provider
import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.example.tarik.data.HistoryItem
import com.example.tarik.data.database.HistoryDao
import com.example.tarik.data.database.HistoryDatabase

private const val BOOKMARKS = 100
private const val BOOKMARK_ID = 101

class HistoryProvider : ContentProvider() {

    private lateinit var historyDao: HistoryDao
    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(
            HistoryContract.AUTHORITY,
            HistoryContract.Bookmarks.PATH_BOOKMARKS,
            BOOKMARKS
        )
        addURI(
            HistoryContract.AUTHORITY,
            "${HistoryContract.Bookmarks.PATH_BOOKMARKS}/#",
            BOOKMARK_ID
        )
    }

    override fun onCreate(): Boolean {
        historyDao = HistoryDatabase.getDatabase(context!!).historyDao()
        return true
    }

    // tells callers what kind of data a URI returns
    override fun getType(uri: Uri): String {
        return when (uriMatcher.match(uri)) {
            BOOKMARKS -> HistoryContract.Bookmarks.CONTENT_TYPE
            BOOKMARK_ID -> HistoryContract.Bookmarks.CONTENT_ITEM_TYPE
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    // query is the read operation and it returns a cursor that the caller iterates
    // we delegate to the DAO which Room implements with a real sqlite cursor
    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        return when (uriMatcher.match(uri)) {
            BOOKMARKS -> historyDao.getAllBookmarksCursor()
            BOOKMARK_ID -> {
                // ContentUris.parseId pulls the integer at the end of /bookmarks/#
                val id = ContentUris.parseId(uri).toInt()
                historyDao.getBookmarkByIdCursor(id)
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        if (uriMatcher.match(uri) != BOOKMARKS) {
            throw IllegalArgumentException("Insert not supported for URI: $uri")
        }
        if (values == null) {
            throw IllegalArgumentException("ContentValues cannot be null")
        }

        // providing safety default for security purposes fro our input.
        val item = HistoryItem(
            title = values.getAsString(HistoryContract.Bookmarks.COLUMN_TITLE) ?: "",
            year = values.getAsString(HistoryContract.Bookmarks.COLUMN_YEAR) ?: "",
            thumbnail = values.getAsString(HistoryContract.Bookmarks.COLUMN_THUMBNAIL),
            extract = values.getAsString(HistoryContract.Bookmarks.COLUMN_EXTRACT) ?: "",
            wikipediaUrl = values.getAsString(HistoryContract.Bookmarks.COLUMN_WIKIPEDIA_URL) ?: "",
            category = values.getAsString(HistoryContract.Bookmarks.COLUMN_CATEGORY) ?: "Uncategorised",
            isBookmarked = true
        )

        val newId: Long = kotlinx.coroutines.runBlocking {
            historyDao.insertItem(item)
        }

        // notify any registered observers that data changed so that UI can auto refresh
        context?.contentResolver?.notifyChange(uri, null)

        // return the URI of the newly created row, with the id appended
        return ContentUris.withAppendedId(HistoryContract.Bookmarks.CONTENT_URI, newId)
    }

    // delete removes a bookmark
    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        if (uriMatcher.match(uri) != BOOKMARK_ID) {
            throw IllegalArgumentException("Delete only supported on a specific id: $uri")
        }

        val id = ContentUris.parseId(uri).toInt()
        val rowsDeleted = historyDao.deleteBookmarkById(id)

        if (rowsDeleted > 0) {
            context?.contentResolver?.notifyChange(uri, null)
        }
        return rowsDeleted
    }

    // update is intentionally not supported we only allow to add or remove a bookmark
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        throw UnsupportedOperationException("Update operation is not supported")
    }
}
