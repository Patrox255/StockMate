package com.example.stockmate.ui.components.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.stockmate.data.util.search.ActiveSort
import com.example.stockmate.data.util.search.FilterGroup
import com.example.stockmate.data.util.search.SortDirection
import com.example.stockmate.data.util.search.SortOption

@Composable
fun <T>FilterSortBar(
    availableSorts: List<SortOption<T>>,
    activeSorts: List<ActiveSort<T>>,
    filterGroups: List<FilterGroup<T>>,
    onFilterOptionToggled: (groupId: String, optionId: String) -> Unit,
    onToggleSort: (SortOption<T>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showFilterMenu by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val canScrollRight by remember { derivedStateOf { listState.canScrollForward } }

    Box(
        modifier = modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {
        LazyRow(
            state = listState,
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(filterGroups) { group ->
                FilterGroupChip(
                    group = group,
                    onOptionToggled = { optionId ->
                        onFilterOptionToggled(group.id, optionId)
                    }
                )
            }

            item {
                VerticalDivider(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            }

            items(availableSorts) { sortOption ->
                val sortIndex = activeSorts.indexOfFirst { it.option == sortOption }
                val isActive = sortIndex != -1
                val activeSort = if (isActive) activeSorts[sortIndex] else null

                FilterChip(
                    selected = isActive,
                    onClick = { onToggleSort(sortOption) },
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(sortOption.displayName)

                            if (isActive && activeSorts.size > 1) {
                                Text(
                                    text = " (${sortIndex + 1})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    trailingIcon = {
                        if (isActive) {
                            val icon = if (activeSort?.direction == SortDirection.ASC) {
                                Icons.Default.ArrowUpward
                            } else {
                                Icons.Default.ArrowDownward
                            }
                            Icon(
                                icon,
                                contentDescription = "Sort direction icon",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                )
            }
        }

        if (canScrollRight) {
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .matchParentSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    ),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Scroll right for more filters",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
    }
}
