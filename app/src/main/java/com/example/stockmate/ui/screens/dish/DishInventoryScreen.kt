package com.example.stockmate.ui.screens.dish

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.stockmate.ui.components.LoadingIndicator
import com.example.stockmate.ui.components.dish.DishListItem
import com.example.stockmate.ui.components.navigation.AddNewFloatingBtn
import com.example.stockmate.ui.components.search.NoItemsFoundMessage
import com.example.stockmate.ui.components.selection.PaginationBar
import com.example.stockmate.ui.viewmodels.dish.DishInventoryViewModel

@Composable
fun DishInventoryScreen(
    viewModel: DishInventoryViewModel = hiltViewModel(),
    onDishClick: (Long) -> Unit,
    onNewDishClick: () -> Unit
) {
    val state by viewModel.dishesPaginationState.collectAsState()
    Log.d("DishInventoryScreen", "isLoading: ${state.isLoading}, error: ${state.error}, items: ${state.items.size}")

    Scaffold(
        floatingActionButton = {
            AddNewFloatingBtn(
                onClick = onNewDishClick,
                contentDescription = "Add a new dish"
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                label = { Text("Search dishes by keyword...") },
                modifier = Modifier
                    .fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (state.items.isEmpty()) {
                NoItemsFoundMessage(
                    msg = if (state.searchQuery.isNotBlank()) {
                        "No dishes match your search"
                    } else {
                        "No dishes found"
                    },
                )
            } else if (state.isLoading) {
                LoadingIndicator()
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = state.items,
                        key = { it.dish.id }
                    ) { dish ->
                        DishListItem(
                            dishWithIngredients = dish,
                            onClick = { onDishClick(dish.dish.id) },
                        )
                    }
                }
            }

            PaginationBar(
                currentPage = state.currentPage,
                visiblePages = state.visiblePages,
                onPageSelected = { viewModel.dishesPaginationManager.goToPage(it) }
            )
        }
    }
}