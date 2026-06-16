package com.example.tarik.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tarik.data.HistoryItem
import com.example.tarik.data.provider.HistoryContract


@Composable
fun ArchiveScreen(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier,
    onCardClick: (Int) -> Unit
) {
    val context = LocalContext.current
    // recollecting whenever the unfiltered list changes such that the archive refreshes the moment the user toggles a bookmark
    val allItems by viewModel.historyItems.collectAsState()
    val bookmarks = allItems.filter { it.isBookmarked }

    if (bookmarks.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No bookmarks yet",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap the bookmark icon on any fact to save it here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(modifier = modifier.fillMaxSize()) {
            items(bookmarks, key = { it.id }) { item ->
                HistoryCard(
                    item = item,
                    onClick = { onCardClick(item.id) }
                )
            }
        }
    }
}
