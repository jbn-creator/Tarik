package com.example.tarik.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.tarik.data.HistoryItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    itemId: Int,
    viewModel: HistoryViewModel,
    onBackClick: () -> Unit,
    onSeeFullArticleClick: (String) -> Unit
) {
    // Localcontext used to get activity context needed to launch the Sharesheet
    val context = LocalContext.current

    // reading from the unfiltered list the article should still be reachable even if the user changes the chip while on this screen
    val historyList by viewModel.historyItems.collectAsState(initial = emptyList())
    val item: HistoryItem = historyList.firstOrNull { it.id == itemId } ?: return

    val sanitisedTitle = item.title.replace(Regex("<.*?>"), "").replace("_", " ")
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = item.year, style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                actions = {
                    // opens share sheet functionality
                    IconButton(onClick = { shareHistoryFact(context, item) }) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share this fact"
                        )
                    }

                    // toggle for (un)bookmarking
                    IconButton(onClick = { viewModel.toggleBookmark(item) }) {
                        Icon(
                            imageVector = if (item.isBookmarked) Icons.Filled.Bookmark
                            else Icons.Filled.BookmarkBorder,
                            contentDescription = if (item.isBookmarked) "Remove bookmark"
                            else "Add bookmark"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            if (item.thumbnail != null) {
                AsyncImage(
                    model = item.thumbnail,
                    contentDescription = sanitisedTitle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    text = sanitisedTitle,
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.year,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    CategoryPill(category = item.category)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = item.extract,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = { onSeeFullArticleClick(item.wikipediaUrl) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Read full article on Wikipedia")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}