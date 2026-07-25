package com.example.stockmate.ui.components.img

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.stockmate.data.util.img.ImageStorage
import com.example.stockmate.data.util.img.ImgDisplayGuidelines

@Composable
fun ImgDisplay(
    absoluteImgPath: String? = null,
    fileName: String? = null,
    currentImageDescription: String,
    noImageNotification: String,
    imgDisplayGuidelines: ImgDisplayGuidelines,
    additionalImgModifier: Modifier = Modifier
) {
    val combinedImgModifier = additionalImgModifier.then(imgDisplayGuidelines.imgModifier)

    val fullImagePath = absoluteImgPath ?: when {
        fileName == null -> null
        fileName.startsWith("http") -> fileName
        else -> ImageStorage(LocalContext.current).getFile(fileName).absolutePath
    }

    if (fullImagePath != null) {
        AsyncImage(
            model = fullImagePath,
            contentDescription = currentImageDescription,
            modifier = combinedImgModifier,
            contentScale = imgDisplayGuidelines.contentScale
        )
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Default.AddPhotoAlternate,
                contentDescription = noImageNotification,
                modifier = imgDisplayGuidelines.iconModifier,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (imgDisplayGuidelines.noImageNotificationVisible) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = noImageNotification,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}