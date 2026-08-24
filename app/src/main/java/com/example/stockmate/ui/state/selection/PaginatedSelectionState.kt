package com.example.stockmate.ui.state.selection

data class PaginatedSelectionState<T>(
    val items: List<T> = emptyList(),
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val visiblePages: List<Int> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)
