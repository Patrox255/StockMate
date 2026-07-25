package com.example.stockmate.ui.components.img


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults.itemShape
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.stockmate.data.util.img.ImgDisplayGuidelines

@Composable
fun ImgPreviewAmongDifferentStyles(
    absoluteImgPath: String? = null,
    fileName: String? = null,
    nameToStyleMap: Map<String, ImgDisplayGuidelines>,
    currentImageDescription: String,
    noImageDescription: String
) {
    var previewType by remember {
        mutableStateOf(nameToStyleMap.keys.firstOrNull() ?: null)
    }
    val previewStyle = when (previewType) {
        null -> null
        else -> nameToStyleMap[previewType]
    }

    SingleChoiceSegmentedButtonRow() {
        nameToStyleMap.keys.forEachIndexed { index, name ->
            SegmentedButton(
                selected = previewType == name,
                onClick = { previewType = name },
                shape = itemShape(index, count=2)
            ) {
                Text(name)
            }
        }
    }

    if (previewStyle == null) {
        throw Error("No preview style found for the selected type: $previewType")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        ImgDisplay(
            fileName = fileName,
            absoluteImgPath = absoluteImgPath,
            imgDisplayGuidelines = previewStyle,
            currentImageDescription = currentImageDescription,
            noImageNotification = noImageDescription
        )
    }

}