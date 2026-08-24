package com.example.stockmate.data.util.selection

import android.util.Log
import com.example.stockmate.data.util.search.SearchSortFilterEngine
import com.example.stockmate.ui.state.selection.PaginatedSelectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PaginatedSelectionManager<T>(
    private val searchEngine: SearchSortFilterEngine<T>,
    private val scope: CoroutineScope,
    private val pageSize: Int = 20
) {
    private val _state = MutableStateFlow(PaginatedSelectionState<T>())
    val state = _state.asStateFlow()

    lateinit var allItems: StateFlow<List<T>>
        private set
    lateinit var currentItems: StateFlow<List<T>>
        private set

    fun initialize(source: Flow<List<T>>) {
        allItems = searchEngine.process(source, scope)
        Log.d("PaginatedSelectionManager", "Initialized with source: ${source}")
        Log.d("PaginatedSelectionManager", "All items flow: ${allItems.value}")

//        combine(
//            allItems,
//            searchEngine.searchQuery
//        ) {items, _ ->
//            val totalPages = calculateTotalPages(items.size)
//            val currentPage = _state.value.currentPage
//                .coerceIn(
//                    0,
//                    (totalPages - 1).coerceAtLeast(0)
//                )
//            Log.d("PaginatedSelectionManager", "Updating page to: $currentPage")
//            val pageItems = getPageItems(items, currentPage)
//            _state.value = _state.value.copy(
//                items = pageItems,
//                currentPage = currentPage,
//                totalPages = totalPages,
//                visiblePages = calculateVisiblePages(
//                    currentPage = currentPage,
//                    totalPages = totalPages
//                )
//            )
//            Log.d("PaginatedSelectionManager", "State updated: ${_state.value}")
//        }.stateIn(
//            scope,
//            started = SharingStarted.WhileSubscribed(5000),
//            initialValue = Unit
//        )
        scope.launch {
            allItems.collect { items ->
                val totalPages = calculateTotalPages(items.size)
                val currentPage = _state.value.currentPage
                    .coerceIn(
                        0,
                        (totalPages - 1).coerceAtLeast(0)
                    )
                Log.d("PaginatedSelectionManager", "Updating page to: $currentPage")
                val pageItems = getPageItems(items, currentPage)
                _state.value = _state.value.copy(
                    items = pageItems,
                    currentPage = currentPage,
                    totalPages = totalPages,
                    visiblePages = calculateVisiblePages(
                        currentPage = currentPage,
                        totalPages = totalPages
                    )
                )
                Log.d("PaginatedSelectionManager", "State updated: ${_state.value}")
            }
        }
    }

    fun updateSearchQuery(query: String) {
        searchEngine.updateSearchQuery(query)
        _state.value = _state.value.copy(
            searchQuery = query
        )
        updatePage(0)
    }
    fun goToPage(page: Int) {
        if (!::allItems.isInitialized) return
        val totalPages = calculateTotalPages(allItems.value.size)
        if (page in 0 until totalPages) {
            updatePage(page)
        }
    }
    fun nextPage() {
        goToPage(_state.value.currentPage + 1)
    }
    fun previousPage() {
        goToPage(_state.value.currentPage - 1)
    }
    fun firstPage() {
        goToPage(0)
    }
    fun lastPage() {
        goToPage(_state.value.totalPages - 1)
    }

    private fun updatePage(page: Int) {
        val items = allItems.value

        _state.value = _state.value.copy(
            currentPage = page,
            items = getPageItems(items, page),
            visiblePages = calculateVisiblePages(
                currentPage = page,
                totalPages = _state.value.totalPages
            )
        )
    }
    private fun getPageItems(
        items: List<T>,
        page: Int
    ): List<T> {
        val fromIndex = page * pageSize
        if (fromIndex >= items.size) {
            return emptyList()
        }
        return items.drop(fromIndex).take(pageSize)
    }
    private fun calculateTotalPages(
        itemCount: Int
    ): Int {
        if (itemCount == 0) return 0
        return (itemCount + pageSize - 1) / pageSize
    }
    private fun calculateVisiblePages(
        currentPage: Int,
        totalPages: Int
    ): List<Int> {
        if (totalPages <= 7) {
            return (0 until totalPages).toList()
        }

        val pages = mutableSetOf<Int>()
        pages.add(0)
        for (page in currentPage - 2..currentPage + 2) {
            if (page in 0 until totalPages) {
                pages.add(page)
            }
        }
        pages.add(totalPages - 1)
        return pages.sorted()
    }
}