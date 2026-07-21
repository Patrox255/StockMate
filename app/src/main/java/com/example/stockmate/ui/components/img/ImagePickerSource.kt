package com.example.stockmate.ui.components.img

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.stockmate.data.util.ImageStorage
import com.example.stockmate.ui.viewmodels.img.LocalGalleryViewModel
import kotlinx.coroutines.launch

interface ImagePickerSource {
    val title: String
    val icon: ImageVector

    @Composable
    fun setupAction(onImagePicked: (String) -> Unit): () -> Unit
}

object LocalGallerySource : ImagePickerSource {
    override val title: String = "Local Gallery"
    override val icon: ImageVector = Icons.Default.PhotoLibrary

    @Composable
    override fun setupAction(onImagePicked: (String) -> Unit): () -> Unit {
        val viewmodel: LocalGalleryViewModel = hiltViewModel()

        val photoPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            uri ?: return@rememberLauncherForActivityResult
            viewmodel.saveImage(uri) { localFileName ->
                if (localFileName != null)
                    onImagePicked(localFileName)
            }
        }

        return {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }
}

object PixabaySource : ImagePickerSource {
    override val title: String = "Pixabay"
    override val icon: ImageVector = Icons.Default.PhotoLibrary

    @Composable
    override fun setupAction(onImagePicked: (String) -> Unit): () -> Unit {
        var showDialog by remember { mutableStateOf(false) }

        if (showDialog) {
            PixabaySearchDialog(
                onImageSelected = { imageUrl ->
                    onImagePicked(imageUrl)
                    showDialog = false
                },
                onDismissRequest = {
                    showDialog = false
                }
            )
        }

        return {
            showDialog = true
        }
    }
}

object OpenFoodFactsSource: ImagePickerSource {
    override val title: String = "OpenFoodFacts"
    override val icon: ImageVector = Icons.Default.PhotoLibrary

    @Composable
    override fun setupAction(onImagePicked: (String) -> Unit): () -> Unit {
        var showDialog by remember {mutableStateOf(false)}

        if (showDialog) {
            OpenFoodFactsSearchDialog(
                onImageSelected = {imageUrl ->
                    onImagePicked(imageUrl)
                    showDialog = false
                },
                onDismissRequest = {
                    showDialog = false
                }
            )
        }

        return {
            showDialog = true
        }
    }
}