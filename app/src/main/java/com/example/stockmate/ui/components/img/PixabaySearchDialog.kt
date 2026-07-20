package com.example.stockmate.ui.components.img

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.stockmate.ui.components.GenericErrorMessage
import com.example.stockmate.ui.viewmodels.img.PixabaySearchViewModel

@Composable
fun PixabaySearchDialog(
    onImageSelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
    viewModel: PixabaySearchViewModel = hiltViewModel()
) {
    val results by viewModel.searchResults.collectAsState()
    val imagesLoadingError = viewModel.imagesLoadingError.collectAsState().value
    val imagesLoading = viewModel.imagesLoading.collectAsState().value
    val isDownloading = viewModel.isDownloading.collectAsState().value
    val downloadingImageError = viewModel.downloadingImageError.collectAsState().value
    val selectedImage = viewModel.selectedImage.collectAsState().value

    var searchQuery by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = if (isDownloading || imagesLoading) {{}} else onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    val image = selectedImage ?: return@TextButton
                    val localFileName = viewModel.downloadSelectedImage(image)

                    if (localFileName != null) {
                        onImageSelected(localFileName)
                    }
                },
                enabled = !isDownloading && !imagesLoading && selectedImage != null
            )
            {
                Text("Confirm")
            }
        },
        dismissButton = {
            if (!isDownloading && !imagesLoading) {
                TextButton(onClick = onDismissRequest) {
                    Text("Cancel")
                }
            }
        },
        title = { Text("Search Pixabay") },
        text = {
            Column(modifier = Modifier.fillMaxSize()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search by a product name...") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isDownloading && !imagesLoading
                )

                Button(
                    onClick = {
                        viewModel.updateSelectedImage(null)
                        viewModel.searchImages(searchQuery)
                    },
                    modifier = Modifier.padding(vertical = 8.dp),
                    enabled = !isDownloading && !imagesLoading
                ) {
                    Text("Search")
                }

                if (isDownloading || imagesLoading)
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                if (imagesLoadingError != null) {
                    GenericErrorMessage(
                        errorMessage = imagesLoadingError,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }

                if (downloadingImageError != null) {
                    GenericErrorMessage(
                        errorMessage = downloadingImageError,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(results) { image ->
                        val isSelected = selectedImage == image

                        ImgDisplay(
                            absoluteImgPath = image.previewURL,
                            currentImageDescription = image.tags,
                            noImageNotification = "No image available",
                            iconModifier = Modifier.size(120.dp),
                            imgModifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (isSelected) 4.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    if (!isDownloading) {
                                        viewModel.updateSelectedImage(image)
                                    }
                                },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    )
}