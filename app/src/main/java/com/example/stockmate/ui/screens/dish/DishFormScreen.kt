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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.stockmate.data.mappers.toUiModel
import com.example.stockmate.data.validationUtil.FieldErrorKey
import com.example.stockmate.ui.components.GenericErrorMessage
import com.example.stockmate.ui.components.TabbedComponent
import com.example.stockmate.ui.components.TabbedComponentEntry
import com.example.stockmate.ui.components.dish.DishDetailsHeader
import com.example.stockmate.ui.components.dish.DishListItem
import com.example.stockmate.ui.components.form.FormTextField
import com.example.stockmate.ui.components.img.ImagePicker
import com.example.stockmate.ui.components.img.LocalGallerySource
import com.example.stockmate.ui.components.img.OpenFoodFactsSource
import com.example.stockmate.ui.components.img.PixabaySource
import com.example.stockmate.ui.components.navigation.NavigateBackDialog
import com.example.stockmate.ui.components.selection.PaginatedSelectionDialog
import com.example.stockmate.ui.viewmodels.dish.DishFormUiEvent
import com.example.stockmate.ui.viewmodels.dish.DishFormViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishFormScreen(
    onNavigateBack: () -> Unit,
    viewModel: DishFormViewModel = hiltViewModel()
) {
    val dish by viewModel.dish.collectAsState()
    val dishErrors by viewModel.dishErrors.collectAsState()
    val globalError by viewModel.dishGlobalError.collectAsState()
    val context = LocalContext.current
    val buttonText = if (viewModel.isEditMode) "Update Dish" else "Create Dish"
    val productsPaginatedSelectionState by viewModel.productsPaginatedSelectionState.collectAsState()
    val multipliersPaginatedSelectionState by viewModel.multipliersPaginatedSelectionState.collectAsState()
    val ingredientsErrors by viewModel.ingredientErrors.collectAsState()
    val dishWithIngredientsForPreview by viewModel.dishWithIngredientsForPreview.collectAsState()
    val isLoadingExistingData by viewModel.isLoadingExistingData.collectAsState()

    var showBackDialog by remember { mutableStateOf(false) }
    var selectingProductIngredientLocalId by remember {mutableStateOf<String?>(null)}
    var selectingMultiplierIngredientLocalId by remember {mutableStateOf<String?>(null)}

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

    if (isLoadingExistingData) {
        CircularProgressIndicator()
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 80.dp)
            ) {
                if (globalError != null) {
                    GenericErrorMessage(
                        errorMessage = globalError ?: "An unknown error occurred.",
                        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                    )
                }

                ImagePicker(
                    currentImagePath = dish.imagePath,
                    onImagePicked = { viewModel.onImageChanged(it) },
                    currentImageDescription = "Current dish image",
                    noImageNotification = "No image selected",
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    availableSources = listOf(
                        LocalGallerySource,
                        PixabaySource,
                        OpenFoodFactsSource
                    ),
                )
                TabbedComponent(
                    entries = listOf(
                        TabbedComponentEntry(
                            title = "Dishes list",
                            content = {
                                DishListItem(
                                    dishWithIngredients = dishWithIngredientsForPreview,
                                )
                            }
                        ),
                        TabbedComponentEntry(
                            title = "Dish details",
                            content = {
                                DishDetailsHeader(
                                    dish = dishWithIngredientsForPreview.dish
                                )
                            }
                        )
                    ),
                    header = "Your ${if (viewModel.isEditMode) "edited" else "new"} dish will look like:"
                )

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

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dish.ingredients.forEach { ingredient ->
                        IngredientCard(
                            uiModel = ingredient.toUiModel(),
                            isEditable = true,
                            onAmountChange = { newAmount ->
                                viewModel.updateIngredient(
                                    ingredient.copy(amountStr = newAmount)
                                )
                            },
                            onProductSelectClick = {
                                selectingProductIngredientLocalId = ingredient.localId
                            },
                            onMultiplierSelectClick = {
                                selectingMultiplierIngredientLocalId = ingredient.localId
                            },
                            onRemoveClick = {
                                viewModel.removeIngredient(ingredient)
                            },
                            validationErrors = mapOf(
                                DishFormViewModel.AddIngredientFormField.PRODUCT to viewModel.ingredientFieldErrors(
                                    ingredientsErrors,
                                    ingredient.localId,
                                    DishFormViewModel.AddIngredientFormField.PRODUCT
                                ),
                                DishFormViewModel.AddIngredientFormField.MULTIPLIER to viewModel.ingredientFieldErrors(
                                    ingredientsErrors,
                                    ingredient.localId,
                                    DishFormViewModel.AddIngredientFormField.MULTIPLIER
                                ),
                                DishFormViewModel.AddIngredientFormField.AMOUNT to viewModel.ingredientFieldErrors(
                                    ingredientsErrors,
                                    ingredient.localId,
                                    DishFormViewModel.AddIngredientFormField.AMOUNT
                                )
                            )
                        )
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
            }

            FloatingActionButton(
                onClick = {
                    viewModel.addEmptyIngredient()
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add an ingredient")
            }

        }
    }

    if (selectingProductIngredientLocalId != null) {
        PaginatedSelectionDialog(
            state = productsPaginatedSelectionState,
            onSearchQueryChanged = { query ->
                viewModel.productsSearchEnginePaginationManager.updateSearchQuery(query)
            },
            onPageSelected = { page ->
                viewModel.productsSearchEnginePaginationManager.goToPage(page)
            },
            onItemSelected = { selectedProduct ->
                viewModel.selectIngredientProduct(
                    localId = selectingProductIngredientLocalId!!,
                    product = selectedProduct
                )
                selectingProductIngredientLocalId = null
            },
            itemLabel = { product -> product.name },
            onDismissRequest = {
                selectingProductIngredientLocalId = null
            },
            dialogHeaderText = "Select a related product",
            itemKeyGenerator = { product -> product.id },
            noItemsFoundMsg = "No products found according to your search query. Please try a different search term."
        )
    }
    if (selectingMultiplierIngredientLocalId != null) {
        PaginatedSelectionDialog(
            state = multipliersPaginatedSelectionState,
            onSearchQueryChanged = { query ->
                viewModel.multipliersSearchEnginePaginationManager.updateSearchQuery(query)
            },
            onPageSelected = { page ->
                viewModel.multipliersSearchEnginePaginationManager.goToPage(page)
            },
            onItemSelected = { selectedMultiplier ->
                val relatedIngredient =
                    dish.ingredients.find { it.localId == selectingMultiplierIngredientLocalId }
                if (relatedIngredient != null) {
                    viewModel.updateIngredient(
                        relatedIngredient.copy(
                            multiplier = selectedMultiplier
                        )
                    )
                    selectingMultiplierIngredientLocalId = null
                }
            },
            itemLabel = { multiplier -> multiplier.name },
            onDismissRequest = {
                selectingMultiplierIngredientLocalId = null
            },
            dialogHeaderText = "Select a related multiplier",
            itemKeyGenerator = { multiplier -> multiplier.id },
            noItemsFoundMsg = "No multipliers found according to your search query. Please try a different search term."
        )
    }
}