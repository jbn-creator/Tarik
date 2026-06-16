package com.example.tarik.data.provider

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryProviderTest {

    private lateinit var context: Context
    private lateinit var resolver: ContentResolver

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        resolver = context.contentResolver

        // start each test from a clean slate
        resolver.query(
            HistoryContract.Bookmarks.CONTENT_URI,
            null, null, null, null
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(HistoryContract.Bookmarks.COLUMN_ID)
            while (cursor.moveToNext()) {
                val id = cursor.getInt(idIndex)
                val itemUri = HistoryContract.Bookmarks.CONTENT_URI
                    .buildUpon().appendPath(id.toString()).build()
                resolver.delete(itemUri, null, null)
            }
        }
    }

    @After
    fun tearDown() {
        // same cleanup as setup so that we leave no residue
        resolver.query(
            HistoryContract.Bookmarks.CONTENT_URI,
            null, null, null, null
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(HistoryContract.Bookmarks.COLUMN_ID)
            while (cursor.moveToNext()) {
                val id = cursor.getInt(idIndex)
                val itemUri = HistoryContract.Bookmarks.CONTENT_URI
                    .buildUpon().appendPath(id.toString()).build()
                resolver.delete(itemUri, null, null)
            }
        }
    }

    @Test
    fun testInsertBookmark_returnsValidUri() {
        val values = sampleBookmarkValues("Battle of Hastings", "1066")
        val newUri = resolver.insert(HistoryContract.Bookmarks.CONTENT_URI, values)

        // the provider should return a non-null URI ending in /<new_id>
        assertNotNull("Insert should return a URI", newUri)
        assertTrue(
            "Returned URI should be under the bookmarks path",
            newUri.toString().startsWith(HistoryContract.Bookmarks.CONTENT_URI.toString())
        )
    }

    @Test
    fun testQueryAllBookmarks_returnsInsertedRow() {
        resolver.insert(
            HistoryContract.Bookmarks.CONTENT_URI,
            sampleBookmarkValues("Moon Landing", "1969")
        )

        // query the full bookmarks list through the resolver
        val cursor = resolver.query(
            HistoryContract.Bookmarks.CONTENT_URI,
            null, null, null, null
        )

        // we should see exactly one row, and its title should match
        assertNotNull("Query should return a cursor", cursor)
        cursor!!.use {
            assertEquals("Should find exactly one bookmark", 1, it.count)
            it.moveToFirst()
            val titleIndex = it.getColumnIndexOrThrow(HistoryContract.Bookmarks.COLUMN_TITLE)
            assertEquals("Moon Landing", it.getString(titleIndex))
        }
    }

    @Test
    fun testDeleteBookmark_removesRow() {
        // insert a bookmark and capture its URI so we can delete it
        val newUri = resolver.insert(
            HistoryContract.Bookmarks.CONTENT_URI,
            sampleBookmarkValues("Berlin Wall Falls", "1989")
        )
        assertNotNull(newUri)

        val rowsDeleted = resolver.delete(newUri!!, null, null)

        // one row should be deleted and the table should now be empty
        assertEquals("Should delete exactly one row", 1, rowsDeleted)

        val cursor = resolver.query(
            HistoryContract.Bookmarks.CONTENT_URI,
            null, null, null, null
        )
        cursor!!.use {
            assertEquals("Bookmarks should be empty after delete", 0, it.count)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testQueryInvalidUri_throws() {
        // any URI that the UriMatcher doesnt recognise should throw
        val badUri = HistoryContract.BASE_CONTENT_URI
            .buildUpon().appendPath("nonsense").build()
        resolver.query(badUri, null, null, null, null)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testUpdateUnsupported_throws() {
        // we deliberately dont support update
        resolver.update(
            HistoryContract.Bookmarks.CONTENT_URI,
            sampleBookmarkValues("anything", "0"),
            null, null
        )
    }

    private fun sampleBookmarkValues(title: String, year: String): ContentValues {
        return ContentValues().apply {
            put(HistoryContract.Bookmarks.COLUMN_TITLE, title)
            put(HistoryContract.Bookmarks.COLUMN_YEAR, year)
            put(HistoryContract.Bookmarks.COLUMN_EXTRACT, "Test extract")
            put(HistoryContract.Bookmarks.COLUMN_WIKIPEDIA_URL, "https://en.wikipedia.org/wiki/Test")
            put(HistoryContract.Bookmarks.COLUMN_CATEGORY, "General")
            put(HistoryContract.Bookmarks.COLUMN_THUMBNAIL, null as String?)
        }
    }
}