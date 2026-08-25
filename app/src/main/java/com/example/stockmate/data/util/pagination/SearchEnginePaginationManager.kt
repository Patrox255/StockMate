package com.example.stockmate.data.util.pagination

import android.util.Log
import com.example.stockmate.data.util.search.SearchSortFilterEngine
import com.example.stockmate.ui.state.pagination.PaginatedState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchEnginePaginationManager<T>(
    private val searchEngine: SearchSortFilterEngine<T>,
    private val scope: CoroutineScope,
    private val pageSize: Int = 20,
    private val errorLoadingItemsMessage: String = "Failed to load items"
) {
    private val _state = MutableStateFlow(PaginatedState<T>())
    val state = _state.asStateFlow()

    private var sourceJob: Job? = null
    // We use this flag to indicate that the current page should be reset to 0 when user
    // changes the search query. We don't reset the page immediately to avoid a flicker in the UI
    // when the search query is updated.
    private var pendingPageReset = false

    lateinit var allItems: StateFlow<List<T>>
        private set

    fun initialize(source: Flow<List<T>>) {
        sourceJob?.cancel()

        _state.value = PaginatedState(
            isLoading = true,
            error = null
        )
        searchEngine.updateSearchQuery("")
        allItems = searchEngine.process(source, scope)

        sourceJob = scope.launch {
            try {
                allItems.collect { items ->
//                    delay(5000)
                    updateStateForItems(items)
                }
                // Due to cancelling the job when the source is re-initialized,
                // we can ignore the cancellation exception here.
            } catch (e: CancellationException) {}
            catch (e: Exception) {
                Log.e("PaginatedSelectionManager", "Error loading items: ${e.message}", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = errorLoadingItemsMessage
                )
            }
        }
    }

    private fun updateStateForItems(items: List<T>) {
        val requestedPage = if (pendingPageReset) 0 else _state.value.currentPage
        pendingPageReset = false

        val totalPages = calculateTotalPages(items.size)
        val currentPage = requestedPage
            .coerceIn(
                0,
                (totalPages - 1).coerceAtLeast(0)
            )
        val pageItems = getPageItems(items, currentPage)
        _state.value = _state.value.copy(
            items = pageItems,
            currentPage = currentPage,
            totalPages = totalPages,
            visiblePages = calculateVisiblePages(
                currentPage = currentPage,
                totalPages = totalPages
            ),
            error = null,
            isLoading = false
        )
    }

    fun updateSearchQuery(query: String) {
        searchEngine.updateSearchQuery(query)
        _state.value = _state.value.copy(
            searchQuery = query,
            isLoading = true,
            items = emptyList()
        )

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