package com.example.stockmate.ui.screens.product

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.camera.camera2.pipe.media.ImageSource
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.stockmate.data.util.img.ProductImgDisplayGuidelines
import com.example.stockmate.ui.components.form.FormTextField
import com.example.stockmate.ui.components.GenericErrorMessage
import com.example.stockmate.ui.components.LoadingIndicator
import com.example.stockmate.ui.components.TabbedComponent
import com.example.stockmate.ui.components.TabbedComponentEntry
import com.example.stockmate.ui.components.img.ImagePicker
import com.example.stockmate.ui.components.img.ImgPreviewAmongDifferentStyles
import com.example.stockmate.ui.components.img.LocalGallerySource
import com.example.stockmate.ui.components.img.OpenFoodFactsSource
import com.example.stockmate.ui.components.img.PixabaySource
import com.example.stockmate.ui.components.navigation.NavigateBack
import com.example.stockmate.ui.components.navigation.NavigateBackDialog
import com.example.stockmate.ui.components.product.InventoryScreenProductItem
import com.example.stockmate.ui.components.product.ProductMultipliersManageFormSection
import com.example.stockmate.ui.viewmodels.ProductFormUiEvent
import com.example.stockmate.ui.viewmodels.ProductFormViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    viewModel: ProductFormViewModel,
    onNavigateBack: () -> Unit
)
{
    val formState by viewModel.formState.collectAsState()
    val focusManager = LocalFocusManager.current
    val errors by viewModel.errors.collectAsState()
    val error by viewModel.error.collectAsState()
    val multipliers by viewModel.multipliersManager.multipliers.collectAsState()
    val isLoadingExistingData by viewModel.isLoadingExistingData.collectAsState()
    val buttonText = if (viewModel.isEditMode) "Update Product" else "Save Product"

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is ProductFormUiEvent.ProductNotFound -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }
                is ProductFormUiEvent.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    }

    if (isLoadingExistingData) {
        LoadingIndicator()
        return
    }

    var showBackDialog by remember {
        mutableStateOf(false)
    }

    fun handleNavigateBack() {
        if (viewModel.hasUnsavedChanges) {
            showBackDialog = true
        } else {
            onNavigateBack()
        }
    }

    BackHandler {
        handleNavigateBack()
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ImagePicker(
                currentImagePath = formState.imagePath,
                onImagePicked = { viewModel.onImageChanged(it) },
                currentImageDescription = "Current product image",
                noImageNotification = "No image selected",
                modifier = Modifier.padding(bottom = 16.dp),
                availableSources = listOf(
                    LocalGallerySource,
                    PixabaySource,
                    OpenFoodFactsSource
                )
            )

            TabbedComponent(
                entries = listOf(
                    TabbedComponentEntry(
                        title = "Inventory item",
                        content = {
                            InventoryScreenProductItem(
                                productWithMultipliers = viewModel.getProductWithMultipliersForPreview(),
                                onNavigateToDetails = {},
                                bottomContent = null
                            )
                        }
                    ),
                    TabbedComponentEntry(
                        title = "Product details",
                        content = {
                            ProductDetailsTopContent(
                                productWithMultipliers = viewModel.getProductWithMultipliersForPreview(),
                                isScrollable = false
                            )
                        }
                    )
                ),
                header = "Your ${if (viewModel.isEditMode) "edited" else "new"} product will look like:"
            )

            FormTextField(
                value = formState.name,
                onValueChange = {viewModel.onNameChanged(it)},
                label = "Product Name",
                errorMessages = errors[ProductFormViewModel.AddProductFormField.NAME] ?: emptyList(),
                modifier = Modifier.fillMaxWidth()
            )

            FormTextField(
                value = formState.unit,
                onValueChange = {viewModel.onUnitChanged(it)},
                label = "Unit (e.g., kg, pcs, l)",
                errorMessages = errors[ProductFormViewModel.AddProductFormField.UNIT] ?: emptyList(),
                modifier = Modifier.fillMaxWidth()
            )

            FormTextField(
                value = formState.targetStock,
                onValueChange = {viewModel.onTargetStockChanged(it)},
                label = "Target Stock",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                errorMessages = errors[ProductFormViewModel.AddProductFormField.TARGET_STOCK] ?: emptyList(),
                modifier = Modifier.fillMaxWidth()
            )

            ProductMultipliersManageFormSection(
                multipliers = multipliers,
                onAddMultiplier = viewModel.multipliersManager::addEmptyMultiplier,
                onUpdateMultiplier = viewModel.multipliersManager::updateMultiplier ,
                onRemoveMultiplier = viewModel.multipliersManager::removeMultiplier,
                modifier = Modifier.padding(top = 16.dp),
                onMultiplierMove = { from, to ->
                    viewModel.multipliersManager.moveMultiplier(from, to)
                }
            )

            Button(
                onClick = {
                    viewModel.saveProduct()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(buttonText)
            }

            if (error != null) {
                GenericErrorMessage(
                    errorMessage = error ?: "An unknown error occurred.",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
fun TabbedComponentEntry(title: String, content: () -> Unit) {
    TODO("Not yet implemented")
}