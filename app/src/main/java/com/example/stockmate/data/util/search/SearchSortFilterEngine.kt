package com.example.stockmate.data.util.search

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds


enum class SortDirection {
    ASC, DESC
}

data class SortOption<T>(
    val id: String,
    val displayName: String,
    val ascendingComparator: Comparator<T>,
    val descendingComparator: Comparator<T> = ascendingComparator.reversed()
)

data class ActiveSort<T>(
    val option: SortOption<T>,
    val direction: SortDirection
)

data class FilterOption<T>(
    val id: String,
    val label: String,
    val predicate: (T) -> Boolean
)

sealed interface FilterGroup<T> {
    val id: String
    val name: String
    val options: List<FilterOption<T>>
    fun appliesTo(item: T): Boolean
}

data class SingleSelectFilterGroup<T>(
    override val id: String,
    override val name: String,
    override val options: List<FilterOption<T>>,
    val selectedOptionId: String? = null
) : FilterGroup<T> {
    override fun appliesTo(item: T): Boolean {
        if (selectedOptionId == null) return true
        val selectedOption = options.find { it.id == selectedOptionId } ?: return true
        return selectedOption.predicate(item)
    }
}

data class MultiSelectFilterGroup<T>(
    override val id: String,
    override val name: String,
    override val options: List<FilterOption<T>>,
    val selectedOptionIds: Set<String> = emptySet()
) : FilterGroup<T> {

    override fun appliesTo(item: T): Boolean {
        if (selectedOptionIds.isEmpty()) return true
        return options
            .filter { selectedOptionIds.contains(it.id) }
            .any { option -> option.predicate(item) }
    }
}

data class SearchResult<T>(
    val items: List<T>,
    val query: String,
    val isInitial: Boolean = false
)

@OptIn(FlowPreview::class)
class SearchSortFilterEngine<T>(
    initialFilterGroups: List<FilterGroup<T>>,
    private val searchMatcher: (item: T, query: String) -> Boolean,
    private val stopTimeoutMillis: Long = 5000L,
    private val debounceDelaySeconds: Double = 0.3
) {
    private val _searchQuery = MutableStateFlow("")
    private val debouncedSearchQuery = _searchQuery
        .debounce(debounceDelaySeconds.seconds)
        .distinctUntilChanged()
    private val _activeSorts = MutableStateFlow<List<ActiveSort<T>>>(emptyList())
    private val _filterGroups = MutableStateFlow(initialFilterGroups)

    val searchQuery = _searchQuery.asStateFlow()
    val filterGroups = _filterGroups.asStateFlow()
    val activeSorts = _activeSorts.asStateFlow()

    fun process(sourceFlow: Flow<List<T>>, scope: CoroutineScope): StateFlow<SearchResult<T>> {
        return combine(
            sourceFlow,
            _activeSorts,
            _filterGroups,
            debouncedSearchQuery
        ) { items, sorts, filters, query ->

            var result = items
            if (query.isNotBlank()) {
                result = result.filter { searchMatcher(it, query) }
            }

            result = result.filter { item ->
                filters.all { group -> group.appliesTo(item) }
            }

            if (sorts.isNotEmpty()) {
                var combinedComparator: Comparator<T>? = null
                for (sort in sorts) {
                    val nextComparator = if (sort.direction == SortDirection.DESC) {
                        sort.option.descendingComparator
                    } else {
                        sort.option.ascendingComparator
                    }
                    combinedComparator = combinedComparator?.then(nextComparator) ?: nextComparator
                }
                combinedComparator?.let { result = result.sortedWith(it) }
            }

            SearchResult(
                items = result,
                query = query,
                isInitial = false
            )
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
            initialValue = SearchResult(items = emptyList(), query = "", isInitial = true)
        )
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSort(option: SortOption<T>) {
        val currentList = _activeSorts.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.option.id == option.id }

        if (existingIndex == -1) {
            currentList.add(ActiveSort(option, SortDirection.ASC))
        } else {
            val existing = currentList[existingIndex]
            val newDirection = when (existing.direction) {
                SortDirection.ASC -> SortDirection.DESC
                SortDirection.DESC -> null
            }
            if (newDirection == null) {
                currentList.removeAt(existingIndex)
            } else {
                currentList[existingIndex] = existing.copy(direction = newDirection)
            }
        }
        _activeSorts.value = currentList
    }

    fun onFilterOptionToggled(groupId: String, optionId: String) {
        _filterGroups.value = _filterGroups.value.map { group ->
            when (group) {
                is SingleSelectFilterGroup -> if (group.id == groupId) {
                    val newOptionId = if (group.selectedOptionId == optionId) null else optionId
                    group.copy(selectedOptionId = newOptionId)
                } else group

                is MultiSelectFilterGroup -> if (group.id == groupId) {
                    val newSelectedIds = if (group.selectedOptionIds.contains(optionId)) {
                        group.selectedOptionIds - optionId
                    } else {
                        group.selectedOptionIds + optionId
                    }
                    group.copy(selectedOptionIds = newSelectedIds)
                } else group
            }
        }
    }
}