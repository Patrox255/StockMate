package com.example.stockmate.ui.components.selection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.example.stockmate.ui.state.selection.PaginatedSelectionState

@Composable
fun <T> PaginatedSelectionDialog(
    state: PaginatedSelectionState<T>,
    onSearchQueryChanged: (String) -> Unit,
    onPageSelected: (Int) -> Unit,
    onItemSelected: (T) -> Unit,
    itemLabel: (T) -> String,
    onDismissRequest: () -> Unit,
    dialogHeaderText: String = "Select"
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

                if (state.isLoading) {
                    CircularProgressIndicator()
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                    ) {
                        items(
                            items = state.items
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