package com.example.stockmate.ui.components.img

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.stockmate.data.remote.img.SearchableImage
import com.example.stockmate.data.remote.img.SearchableImageSearchResult
import com.example.stockmate.data.util.img.ProductImgDisplayGuidelines
import com.example.stockmate.ui.components.GenericErrorMessage
import com.example.stockmate.ui.viewmodels.img.BaseImageSearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : SearchableImage, Y : SearchableImageSearchResult<T>> ImageSearchDialog(
    title: String,
    searchFieldLabel: String,
    viewModel: BaseImageSearchViewModel<T, Y>,
    onImageSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val results by viewModel.results.collectAsState()
    val hasMorePages by viewModel.hasMorePages.collectAsState()
    val pageSize by viewModel.pageSize.collectAsState()
    val imagesLoadingError by viewModel.imagesLoadingError.collectAsState()
    val imagesLoading by viewModel.imagesLoading.collectAsState()
    val isLoadingNextPage by viewModel.isLoadingNextPage.collectAsState()
    val isDownloading by viewModel.isDownloading.collectAsState()
    val downloadingImageError by viewModel.downloadingImageError.collectAsState()
    val selectedImage by viewModel.selectedImage.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var pageSizeMenuExpanded by remember { mutableStateOf(false) }
    val gridState = rememberLazyGridState()
    val busy = isDownloading || imagesLoading

    LaunchedEffect(gridState, results) {
        snapshotFlow {
            val layoutInfo = gridState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index
            lastVisible != null && lastVisible >= layoutInfo.totalItemsCount -4
        }
            .collect{ nearEnd ->
                if (nearEnd && results.isNotEmpty())
                    viewModel.loadNextPage()
            }
    }

    AlertDialog(
        onDismissRequest = if (busy) {{}} else onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    val image = selectedImage ?: return@TextButton
                    viewModel.downloadSelectedImage(image) { localPath ->
                        onImageSelected(localPath)
                    }
                },
                enabled = !busy && selectedImage != null
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            if (!busy) {
                TextButton(onClick = onDismissRequest) { Text("Cancel") }
            }
        },
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.fillMaxSize()) {

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(searchFieldLabel) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !busy
                )

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = pageSizeMenuExpanded,
                    onExpandedChange = { if (!busy) pageSizeMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = pageSize.toString(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Per page") },
                        modifier = Modifier.menuAnchor().width(110.dp),
                        enabled = !busy,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pageSizeMenuExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = pageSizeMenuExpanded,
                        onDismissRequest = { pageSizeMenuExpanded = false }
                    ) {
                        viewModel.availablePageSizes.forEach { size ->
                            DropdownMenuItem(
                                text = { Text(size.toString()) },
                                onClick = {
                                    viewModel.updatePageSize(size)
                                    pageSizeMenuExpanded = false
                                }
                            )
                        }
                    }
                }


                Button(
                    onClick = {
                        viewModel.updateSelectedImage(null)
                        viewModel.searchImages(searchQuery)
                    },
                    modifier = Modifier.padding(vertical = 8.dp),
                    enabled = !busy && searchQuery.isNotBlank()
                ) {
                    Text("Search")
                }

                if (busy) {
                    Text(
                        text = if (isDownloading) "Gathering the image..." else "Loading images...",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 8.dp)
                    )
                }

                imagesLoadingError?.let {
                    GenericErrorMessage(errorMessage = it, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
                }
                downloadingImageError?.let {
                    GenericErrorMessage(errorMessage = it, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    state = gridState
                ) {
                    items(results) { image ->
                        val isSelected = selectedImage == image

                        ImgDisplay(
                            absoluteImgPath = image.previewUrl,
                            currentImageDescription = image.description,
                            noImageNotification = "No image available",
                            additionalImgModifier = Modifier
                                .border(
                                    width = if (isSelected) 4.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { if (!isDownloading) viewModel.updateSelectedImage(image) },
                            imgDisplayGuidelines = ProductImgDisplayGuidelines.ImgSearchDialog
                        )
                    }

                    if (isLoadingNextPage) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    )
}