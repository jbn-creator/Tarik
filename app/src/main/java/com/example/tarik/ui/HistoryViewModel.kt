package com.example.tarik.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tarik.TarikApplication
import com.example.tarik.data.HistoryItem
import com.example.tarik.data.HistoryRepository
import com.example.tarik.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate


data class HistoryUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

const val CATEGORY_ALL = "All"


class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HistoryRepository =
        (application as TarikApplication).repository

    // settings repository injected so we can react to weight changes
    private val settingsRepo: SettingsRepository =
        (application as TarikApplication).settingsRepository

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _selectedCategory = MutableStateFlow(CATEGORY_ALL)
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val historyItems: StateFlow<List<HistoryItem>> = repository.allHistory.map { items -> items.distinctBy { it.title } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // combine joins three flows: the raw items the category filter and the user's category weights. the result is filtered then sorted by user preference
    val filteredItems: StateFlow<List<HistoryItem>> =
        combine(historyItems, _selectedCategory, settingsRepo.categoryWeights) { items, category, weights ->
            val filtered = if (category == CATEGORY_ALL) items
            else items.filter { it.category == category }

            // sort by user-assigned weight
            // ties (many articles part of the same class) broken by year with the more recent being displayed first
            filtered.sortedWith(
                compareByDescending<HistoryItem> { weights[it.category] ?: 0.5f }
                    .thenByDescending { it.year.toIntOrNull() ?: 0 }
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarks: StateFlow<List<HistoryItem>> = historyItems
        .map { items -> items.filter { it.isBookmarked } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshTodayHistory()
    }

    fun refreshTodayHistory() {
        val today = LocalDate.now()
        val month = today.monthValue.toString().padStart(2, '0')
        val day = today.dayOfMonth.toString().padStart(2, '0')

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                repository.refreshWikipediaHistory(month, day)
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Couldn't fetch history. Showing cached events."
                )
            }
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun toggleBookmark(item: HistoryItem) {
        viewModelScope.launch {
            repository.toggleBookmark(item)
        }
    }
}
