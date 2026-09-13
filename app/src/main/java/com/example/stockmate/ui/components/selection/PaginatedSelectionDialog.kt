package com.example.stockmate.ui.components.selection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stockmate.ui.components.GenericErrorMessage
import com.example.stockmate.ui.components.LoadingIndicator
import com.example.stockmate.ui.components.search.NoItemsFoundMessage
import com.example.stockmate.ui.state.pagination.PaginatedState

@Composable
fun <T> PaginatedSelectionDialog(
    state: PaginatedState<T>,
    onSearchQueryChanged: (String) -> Unit,
    onPageSelected: (Int) -> Unit,
    onItemSelected: (T) -> Unit,
    itemLabel: (T) -> String,
    onDismissRequest: () -> Unit,
    dialogHeaderText: String = "Select",
    noItemsFoundMsg: String = "No items found",
    itemKeyGenerator: ((T) -> Any)? = null
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(dialogHeaderText)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = onSearchQueryChanged,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = {
                        Text("Search")
                    }
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                when {
                    state.isLoading -> {
                        LoadingIndicator()
                    }
                    state.error != null -> {
                        GenericErrorMessage(
                            errorMessage = state.error
                        )
                    }
                    state.items.isEmpty() -> {
                        NoItemsFoundMessage(
                            msg = noItemsFoundMsg
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(350.dp)
                        ) {
                            items(
                                items = state.items,
                                key = itemKeyGenerator
                            ) { item ->
                                Text(
                                    text = itemLabel(item),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onItemSelected(item)
                                        }
                                        .padding(
                                            horizontal = 12.dp,
                                            vertical = 12.dp
                                        )
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )
                PaginationBar(
                    currentPage = state.currentPage,
                    visiblePages = state.visiblePages,
                    onPageSelected = onPageSelected
                )
            }
        },

        confirmButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text("Cancel")
            }
        }
    )
}