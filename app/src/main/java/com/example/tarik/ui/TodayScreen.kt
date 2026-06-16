package com.example.tarik.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier


@Composable
fun TodayScreen(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier,
    onCardClick: (Int) -> Unit
) {
    // collecting the filtered list viewmodel applies the category filter for us
    val items by viewModel.filteredItems.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    Column(modifier = modifier) {

        CategoryFilterBar(
            selectedCategory = selectedCategory,
            onCategorySelected = { viewModel.selectCategory(it) }
        )

        LazyColumn {
            // key = it.id so LazyColumn preserves scroll position across filter changes
            items(items, key = { it.id }) { item ->
                HistoryCard(
                    item = item,
                    onClick = { onCardClick(item.id) }
                )
            }
        }
    }
}
