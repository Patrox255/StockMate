package com.example.stockmate.ui.components.dish

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.stockmate.data.entity.Dish
import com.example.stockmate.data.entity.IngredientWithProductAndMultiplier
import com.example.stockmate.data.mappers.toUiModel
import com.example.stockmate.data.util.img.DishImgDisplayGuidelines
import com.example.stockmate.ui.components.img.ImgDisplay
import com.example.stockmate.ui.screens.dish.IngredientCard

@Composable
fun DishDetailsHeader(
    dish: Dish,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ImgDisplay(
            fileName = dish.imagePath,
            currentImageDescription = dish.name,
            noImageNotification = "No image available",
            imgDisplayGuidelines = DishImgDisplayGuidelines.DishDetails
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = dish.name,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (!dish.description.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = dish.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DishIngredientsSection(
    ingredients: List<IngredientWithProductAndMultiplier>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Ingredients (${ingredients.size})",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        if (ingredients.isEmpty()) {
            Text(
                text = "No ingredients added yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            ingredients.forEach { ingredientData ->
                IngredientCard(
                    uiModel = ingredientData.toUiModel()
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}