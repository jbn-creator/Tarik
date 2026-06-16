package com.example.tarik.data
import androidx.room.Entity
import androidx.room.PrimaryKey

// Room entity representing a single history event in our database
// One row in the history_table corresponds to one Wikipedia article we showed today
@Entity(tableName = "history_table")
data class HistoryItem(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,        // Headline from Wikipedia
    val year: String,         // The year it happened
    val thumbnail: String?,   // Image URL (we use the ? since it can be empty/null)
    val extract: String,      // The 3-4 paragraph article shown when clicking the card
    val wikipediaUrl: String, // The link to the full article
    val category: String = "Uncategorised", // category of article according to algorithm
    val isBookmarked: Boolean = false
)
