package com.example.stockmate.ui.components.img

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.stockmate.data.util.ImageStorage
import com.example.stockmate.ui.viewmodels.img.ImagePickerViewModel

@Composable
fun ImagePicker(
    currentImagePath: String?,
    onImagePicked: (String) -> Unit,
    availableSources: List<ImagePickerSource> = listOf(LocalGallerySource),
    modifier: Modifier = Modifier,
    currentImageDescription: String,
    noImageNotification: String,
    viewmodel: ImagePickerViewModel = hiltViewModel()
) {
    val sourceActions = availableSources.associateWith { source ->
        key(source.title) {
            source.setupAction(onImagePicked)
        }
    }

    var showSourceSelector by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable {
                if (availableSources.size == 1) {
                    sourceActions[availableSources.first()]?.invoke()
                } else if (availableSources.isNotEmpty()) {
                    showSourceSelector = true
                }
            },
        contentAlignment = Alignment.Center
    ) {
        ImgDisplay(
            absoluteImgPath = if (currentImagePath != null)
                viewmodel.getImgAbsolutePathBasedOnDeviceStorage(currentImagePath) else null,
            currentImageDescription = currentImageDescription,
            noImageNotification = noImageNotification,
            contentScale = ContentScale.Crop
        )
    }

    if (showSourceSelector) {
        ImageSourceSelectorSheet(
            sources = availableSources,
            onDismiss = { showSourceSelector = false },
            onSourceSelected = { source ->
                showSourceSelector = false

                sourceActions[source]?.invoke()
            }
        )
    }
}