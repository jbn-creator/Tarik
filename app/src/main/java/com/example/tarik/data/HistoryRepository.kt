package com.example.tarik.data

import android.util.Log
import com.example.tarik.data.classifier.BayesianClassifier
import com.example.tarik.data.database.HistoryDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL


class HistoryRepository(
    private val historyDao: HistoryDao,
    private val classifier: BayesianClassifier
) {

    val allHistory: Flow<List<HistoryItem>> = historyDao.getAllHistory()

    suspend fun refreshWikipediaHistory(month: String, day: String) = withContext(Dispatchers.IO) {

        try {
            //making a request for the articles (we need to include user agent as part of our header to get a valid response from wikipedia)
            val url = URL("https://en.wikipedia.org/api/rest_v1/feed/onthisday/all/$month/$day")
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "TarikHistoryApp/1.0")


            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val rawJson = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(rawJson)
                val selected = json.optJSONArray("selected") ?: return@withContext

                val historyList = mutableListOf<HistoryItem>()

                for (i in 0 until selected.length()) {
                    val event = selected.getJSONObject(i)
                    val page = event.optJSONArray("pages")?.optJSONObject(0)

                    if (page != null) {
                        val extractText = page.optString("extract")

                        // classifying the articles using our classifier
                        val classification = classifier.classify(extractText)

                        historyList.add(
                            HistoryItem(
                                title = page.optString("title"),
                                year = event.optString("year"),
                                thumbnail = page.optJSONObject("thumbnail")?.optString("source"),
                                extract = extractText,
                                wikipediaUrl = page.optJSONObject("content_urls")
                                    ?.optJSONObject("mobile")?.optString("page") ?: "",
                                category = classification.category
                            )
                        )
                    }
                }

                // Clear non bookmarked articles of previous days feed before inserting fresh data to avoid duplicate rows on refresh.
                historyDao.clearDailyFeed()
                historyDao.insertAll(historyList)
            }
        } catch (e: Exception) {
            Log.e("HistoryRepository", "Refresh error: ${e.message}")
        }
    }

    suspend fun toggleBookmark(item: HistoryItem) {
        historyDao.updateItem(item.copy(isBookmarked = !item.isBookmarked))
    }
}
