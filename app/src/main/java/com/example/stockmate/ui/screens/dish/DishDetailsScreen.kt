package com.example.stockmate.ui.screens.dish

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.stockmate.ui.components.GenericErrorMessage
import com.example.stockmate.ui.components.LoadingIndicator
import com.example.stockmate.ui.components.dish.CookDishDialog
import com.example.stockmate.ui.components.dish.DishDetailsHeader
import com.example.stockmate.ui.components.dish.DishIngredientsSection
import com.example.stockmate.ui.viewmodels.dish.DishDetailsUiEvent
import com.example.stockmate.ui.viewmodels.dish.DishDetailsUiState
import com.example.stockmate.ui.viewmodels.dish.DishDetailsViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishDetailsScreen(
    onEditDishClick: (Long) -> Unit,
    viewModel: DishDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showCookDialog by viewModel.showCookDialog.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is DishDetailsUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        when (val state = uiState) {
            is DishDetailsUiState.Loading -> {
                LoadingIndicator()
            }

            is DishDetailsUiState.Error -> {
                GenericErrorMessage(
                    errorMessage = state.message,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.Center)
                )
            }

            is DishDetailsUiState.Success -> {
                val data = state.dishWithIngredients
                val dish = data.dish
                val ingredients = data.ingredients

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .padding(bottom = 80.dp)
                ) {
                    DishDetailsHeader(
                        dish = dish
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    DishIngredientsSection(
                        ingredients = ingredients
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { viewModel.onCookClick() },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Icon(Icons.Default.RestaurantMenu, contentDescription = "Cook the dish")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cook this dish")
                    }
                }

                FloatingActionButton(
                    onClick = { onEditDishClick(dish.id) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit the dish")
                }

                if (showCookDialog) {
                    CookDishDialog(
                        dishName = dish.name,
                        ingredients = ingredients,
                        onConfirm = { viewModel.confirmCook(data) },
                        onDismiss = { viewModel.onDismissCookDialog() }
                    )
                }
            }
        }
    }

}