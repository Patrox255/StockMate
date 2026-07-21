package com.example.stockmate.ui.components.img

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.stockmate.ui.viewmodels.img.OpenFoodFactsViewModel
import com.example.stockmate.ui.viewmodels.img.PixabaySearchViewModel

@Composable
fun PixabaySearchDialog(
    onImageSelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
    viewModel: PixabaySearchViewModel = hiltViewModel()
) = ImageSearchDialog(
    title = "Pixabay Image Search",
    searchFieldLabel = "Search for images",
    viewModel = viewModel,
    onImageSelected = onImageSelected,
    onDismissRequest = onDismissRequest
)

@Composable
fun OpenFoodFactsSearchDialog(
    onImageSelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
    viewModel: OpenFoodFactsViewModel = hiltViewModel()
) = ImageSearchDialog(
    title = "Open Food Facts Image Search",
    searchFieldLabel = "Search for products",
    viewModel = viewModel,
    onImageSelected = onImageSelected,
    onDismissRequest = onDismissRequest
)