package com.example.stockmate.data.util.img

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

data class ImgDisplayGuidelines(
    val contentScale: ContentScale,
    val imgModifier: Modifier,
    val iconModifier: Modifier,
    val noImageNotificationVisible: Boolean,
)

object ProductImgDisplayGuidelines {
    val InventoryItem = ImgDisplayGuidelines(
        contentScale = ContentScale.Crop,
        imgModifier = Modifier.size(64.dp),
        iconModifier = Modifier.size(48.dp),
        noImageNotificationVisible = false,
    )

    val ProductDetails = ImgDisplayGuidelines(
        contentScale = ContentScale.Fit,
        imgModifier = Modifier.size(200.dp),
        iconModifier = Modifier.size(64.dp),
        noImageNotificationVisible = true,
    )

    val FormImagePicker = ImgDisplayGuidelines(
        contentScale = ContentScale.Fit,
        imgModifier = Modifier.size(120.dp),
        iconModifier = Modifier.size(64.dp),
        noImageNotificationVisible = true,
    )

    val ImgSearchDialog = FormImagePicker.copy(
        iconModifier = Modifier.size(120.dp),
        imgModifier = Modifier.size(120.dp)
            .clip(RoundedCornerShape(8.dp)),
    )
}