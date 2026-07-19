package com.example.stockmate.ui.screens.product

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.stockmate.ui.components.FormTextField
import com.example.stockmate.ui.components.GenericErrorMessage
import com.example.stockmate.ui.components.navigation.NavigateBack
import com.example.stockmate.ui.components.navigation.NavigateBackDialog
import com.example.stockmate.ui.components.product.ProductMultipliersManageFormSection
import com.example.stockmate.ui.viewmodels.AddProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    viewModel: AddProductViewModel,
    onNavigateBack: () -> Unit
)
{
    val formState by viewModel.formState.collectAsState()
    val focusManager = LocalFocusManager.current
    val errors by viewModel.errors.collectAsState()
    val error by viewModel.error.collectAsState()
    val multipliers by viewModel.multipliersManager.multipliers.collectAsState()

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

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            },
        topBar = {
            TopAppBar(
                title = { Text("Add a new product") },
                navigationIcon = {
                    NavigateBack(onNavigateBack = {
                        handleNavigateBack()
                    })
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())

        ) {
            FormTextField(
                value = formState.name,
                onValueChange = {viewModel.onNameChanged(it)},
                label = "Product Name",
                errorMessages = errors[AddProductViewModel.AddProductFormField.NAME] ?: emptyList()
            )

            FormTextField(
                value = formState.unit,
                onValueChange = {viewModel.onUnitChanged(it)},
                label = "Unit (e.g., kg, pcs, l)",
                errorMessages = errors[AddProductViewModel.AddProductFormField.UNIT] ?: emptyList()
            )

            FormTextField(
                value = formState.targetStock,
                onValueChange = {viewModel.onTargetStockChanged(it)},
                label = "Target Stock",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                errorMessages = errors[AddProductViewModel.AddProductFormField.TARGET_STOCK] ?: emptyList()
            )

            FormTextField(
                value = formState.packageSize,
                onValueChange = { viewModel.onPackageSizeChanged(it) },
                label = "Package Size",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                errorMessages = errors[AddProductViewModel.AddProductFormField.PACKAGE_SIZE] ?: emptyList()
            )

            ProductMultipliersManageFormSection(
                multipliers = multipliers,
                onAddMultiplier = viewModel.multipliersManager::addEmptyMultiplier,
                onUpdateMultiplier = viewModel.multipliersManager::updateMultiplier ,
                onRemoveMultiplier = viewModel.multipliersManager::removeMultiplier,
                modifier = Modifier.padding(top = 16.dp)
            )

            Button(
                onClick = {
                    viewModel.saveProduct(onSuccess = onNavigateBack)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Product")
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