package com.example.stockmate.ui.components.product

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stockmate.data.entity.ProductWithMultipliers
import com.example.stockmate.data.util.img.ProductImgDisplayGuidelines
import com.example.stockmate.ui.components.img.ImgDisplay
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale

@Composable
fun InventoryScreenProductItem(
    productWithMultipliers: ProductWithMultipliers,
    onNavigateToDetails: (productId: Long) -> Unit,
    bottomContent: @Composable ((productWithMultipliers: ProductWithMultipliers) -> Unit)? = null
) {
    val (product, multipliers) = productWithMultipliers

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToDetails(product.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    ImgDisplay(
                        fileName = product.imageUrl,
                        currentImageDescription = "Image of ${product.name}",
                        noImageNotification = "No image available for ${product.name}",
                        imgDisplayGuidelines = ProductImgDisplayGuidelines.InventoryItem
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = product.name, style = MaterialTheme.typography.titleLarge)

                    Spacer(modifier = Modifier.height(4.dp))

                    val isLowStock = product.currentStock < product.targetStock
                    Row {
                        Text(
                            text = "Stock: ${product.currentStock}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = " / ${product.targetStock} ${product.unit}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LinearProgressIndicator(
                            progress = { (product.stockPercentage / 100).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(3.dp)
                                )
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )

                        Text(
                            text = "${String.format(LocalLocale.current.platformLocale, "%.1f", product.stockPercentage)}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            bottomContent?.invoke(productWithMultipliers)
        }
    }
}