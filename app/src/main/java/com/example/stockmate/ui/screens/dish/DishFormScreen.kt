package com.example.stockmate.ui.screens.dish

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.stockmate.data.mappers.toUiModel
import com.example.stockmate.data.validationUtil.FieldErrorKey
import com.example.stockmate.ui.components.GenericErrorMessage
import com.example.stockmate.ui.components.form.FormTextField
import com.example.stockmate.ui.components.navigation.NavigateBackDialog
import com.example.stockmate.ui.components.selection.PaginatedSelectionDialog
import com.example.stockmate.ui.viewmodels.dish.DishFormUiEvent
import com.example.stockmate.ui.viewmodels.dish.DishFormViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishFormScreen(
    onNavigateBack: () -> Unit,
    viewModel: DishFormViewModel = hiltViewModel()
) {
    val dish by viewModel.dish.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val dishErrors by viewModel.dishErrors.collectAsState()
    val globalError by viewModel.dishGlobalError.collectAsState()
    val context = LocalContext.current
    val buttonText = if (viewModel.isEditMode) "Update Dish" else "Create Dish"
    val productsPaginatedSelectionState by viewModel.productsPaginatedSelectionState.collectAsState()

    var showBackDialog by remember { mutableStateOf(false) }
    var selectingProductIngredientLocalId by remember {mutableStateOf<String?>(null)}

    val closeSheet = {
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {

        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is DishFormUiEvent.NavigateBack -> {
                    onNavigateBack()
                }
                is DishFormUiEvent.DishNotFound -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }
            }
        }
    }

    fun handleNavigateBack() {
        if (viewModel.hasUnsavedChanges)
            showBackDialog = true
        else
            onNavigateBack()
    }

    if (showBackDialog) {
        NavigateBackDialog(
            onConfirm = {
                showBackDialog = false
                onNavigateBack()
            },
            onDismiss = {
                showBackDialog = false
            }
        )
    }

    BackHandler {
        handleNavigateBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        if (globalError != null) {
            GenericErrorMessage(
                errorMessage = globalError ?: "An unknown error occurred.",
                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
            )
        }

        FormTextField(
            value = dish.name,
            onValueChange = { viewModel.onNameChanged(it) },
            label = "Name",
            modifier = Modifier.fillMaxWidth(),
            errorMessages = dishErrors[FieldErrorKey(
                field = DishFormViewModel.AddDishFormField.NAME
            )] ?: emptyList()
        )

        Spacer(modifier = Modifier.height(8.dp))

        FormTextField(
            value = dish.description,
            onValueChange = { viewModel.onDescriptionChanged(it) },
            label = "Description",
            modifier = Modifier.fillMaxWidth(),
            errorMessages = dishErrors[FieldErrorKey(
                field = DishFormViewModel.AddDishFormField.DESCRIPTION
            )] ?: emptyList()
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Ingredients",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dish.ingredients, key = { it.localId }) { ingredient ->
                    IngredientCard(
                        uiModel = ingredient.toUiModel(),
                        isEditable = true,
                        onAmountChange = { newAmount ->
                            viewModel.updateIngredient(
                                ingredient.copy(amount = newAmount.toDoubleOrNull() ?: 0.0)
                            )
                        },
                        onProductSelectClick = {
                            selectingProductIngredientLocalId = ingredient.localId
                        },
                        onMultiplierSelectClick = {

                        },
                        onRemoveClick = {
                            viewModel.removeIngredient(ingredient)
                        },
                        validationErrors = mapOf(
                            DishFormViewModel.AddIngredientFormField.PRODUCT to viewModel.getIngredientFieldErrors(
                                ingredient.localId, DishFormViewModel.AddIngredientFormField.PRODUCT
                            ),
                            DishFormViewModel.AddIngredientFormField.MULTIPLIER to viewModel.getIngredientFieldErrors(
                                ingredient.localId,
                                DishFormViewModel.AddIngredientFormField.MULTIPLIER
                            ),
                            DishFormViewModel.AddIngredientFormField.AMOUNT to viewModel.getIngredientFieldErrors(
                                ingredient.localId, DishFormViewModel.AddIngredientFormField.AMOUNT
                            )
                        )
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }

            }

            FloatingActionButton(
                onClick = {
                    viewModel.addEmptyIngredient()
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add an ingredient")
            }
        }

        Button(
            onClick = {
                viewModel.saveDish()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text(buttonText)
        }
    }

    if (selectingProductIngredientLocalId != null) {
        PaginatedSelectionDialog(
            state = productsPaginatedSelectionState,
            onSearchQueryChanged = { query ->
                viewModel.productsPaginatedSelectionManager.updateSearchQuery(query)
            },
            onPageSelected = { page ->
                viewModel.productsPaginatedSelectionManager.goToPage(page)
            },
            onItemSelected = {selectedProduct ->
                val relatedIngredient = dish.ingredients.find { it.localId == selectingProductIngredientLocalId }
                if (relatedIngredient != null) {
                    viewModel.updateIngredient(
                        relatedIngredient.copy(
                            product = selectedProduct
                        )
                    )
                }

            },
            itemLabel = { product -> product.name },
            onDismissRequest = {
                selectingProductIngredientLocalId = null
            },
            dialogHeaderText = "Select a related product",
        )
    }
}