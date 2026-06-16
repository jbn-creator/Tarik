package com.example.tarik.data.provider

import android.net.Uri

object HistoryContract {


    const val AUTHORITY = "com.example.tarik.provider"

    // base URI for everything our provider exposes
    val BASE_CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY")

    // only expose bookmarks externally so the daily cache stays private
    // since its just transient API data with no value to other apps
    object Bookmarks {

        const val PATH_BOOKMARKS = "bookmarks"

        // full URI for the bookmarks table
        val CONTENT_URI: Uri = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKMARKS)

        // MIME types that tell other apps what shape of data to expect

        const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.$AUTHORITY.$PATH_BOOKMARKS"
        const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.$AUTHORITY.$PATH_BOOKMARKS"

        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_YEAR = "year"
        const val COLUMN_THUMBNAIL = "thumbnail"
        const val COLUMN_EXTRACT = "extract"
        const val COLUMN_WIKIPEDIA_URL = "wikipediaUrl"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_IS_BOOKMARKED = "isBookmarked"
    }
}
