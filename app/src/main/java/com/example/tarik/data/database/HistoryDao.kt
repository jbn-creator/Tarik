package com.example.tarik.data.database

import android.database.Cursor
import androidx.room.*
import com.example.tarik.data.HistoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    // sorting by year ensures the home feed looks organised
    @Query("SELECT * FROM history_table ORDER BY year DESC")
    fun getAllHistory(): Flow<List<HistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<HistoryItem>)

    @Update
    suspend fun updateItem(item: HistoryItem)

    @Query("DELETE FROM history_table WHERE isBookmarked = 0")
    suspend fun clearDailyFeed()

    @Query("SELECT * FROM history_table WHERE isBookmarked = 1")
    fun getAllBookmarksCursor(): Cursor

    @Query("SELECT * FROM history_table WHERE id = :id AND isBookmarked = 1")
    fun getBookmarkByIdCursor(id: Int): Cursor

    @Query("DELETE FROM history_table WHERE id = :id AND isBookmarked = 1")
    fun deleteBookmarkById(id: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: HistoryItem): Long

    @Query("SELECT * FROM history_table WHERE id = :id")
    suspend fun getById(id: Int): HistoryItem?
}