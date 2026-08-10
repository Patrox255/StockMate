package com.example.stockmate.ui.screens.product

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.stockmate.data.entity.ProductWithMultipliers
import com.example.stockmate.data.util.img.ProductImgDisplayGuidelines
import com.example.stockmate.ui.components.LoadingIndicator
import com.example.stockmate.ui.components.form.DeleteConfirmationDialog
import com.example.stockmate.ui.components.img.ImgDisplay
import com.example.stockmate.ui.components.navigation.NavigateBack
import com.example.stockmate.ui.components.product.ProductNoMultipliersConfiguredMessage
import com.example.stockmate.ui.components.product.ProductPredictionCard
import com.example.stockmate.ui.components.product.SelectStockAdjustmentReasonDialog
import com.example.stockmate.ui.components.product.StockAdjustmentControls
import com.example.stockmate.ui.viewmodels.ProductDetailsViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    viewModel: ProductDetailsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    onTitleUpdate: (String?) -> Unit,
    onActionsUpdate: ((@Composable RowScope.() -> Unit)?) -> Unit,
) {
    val productDetailsState by viewModel.productDetails.collectAsState()
    val details = productDetailsState

    if (details == null) {
        LoadingIndicator()
        return
    }

    val focusManager = LocalFocusManager.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            title = "Delete ${details.product.name}",
            message = "Are you sure you want to delete this product? This action cannot be undone.",
            onConfirm = {
                viewModel.deleteProduct()
                showDeleteDialog = false
                onNavigateBack()
            },
            onDismiss = {
                showDeleteDialog = false
            }
        )
    }

    LaunchedEffect(productDetailsState) {
        productDetailsState?.product?.name?.let { productName ->
            onTitleUpdate(productName)
        }
        onActionsUpdate({
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = null)
            }
            IconButton(onClick = onNavigateToEdit) {
                Icon(Icons.Default.Edit, contentDescription = null)
            }
        })
    }

    Box(
        // In order to make it so that when a user clicks outside the Text Field then it counts as
        // lose of the focus
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
    ) {
        ProductDetailsContent(
            productWithMultipliers = details,
            viewModel = viewModel
        )
    }
}

@Composable
fun ProductDetailsTopContent(
    productWithMultipliers: ProductWithMultipliers,
    modifier: Modifier = Modifier,
    content: @Composable ((productWithMultipliers: ProductWithMultipliers) -> Unit)? = null,
    isScrollable: Boolean = true
) {
    val (product, _) = productWithMultipliers
    val columnModifier = modifier
        .fillMaxSize()
        .padding(16.dp)
    val finalColumnModifier = if (isScrollable) {
        columnModifier.verticalScroll(rememberScrollState())
    } else {
        columnModifier
    }

    Column(
        modifier = finalColumnModifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${product.currentStock} ${product.unit}",
                style = MaterialTheme.typography.displayMedium
            )
            Text(
                text = "Goal: ${product.targetStock} ${product.unit}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(bottom = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            ImgDisplay(
                fileName = product.imageUrl,
                currentImageDescription = "Product Image",
                noImageNotification = "No image available. Head to edit to add one.",
                imgDisplayGuidelines = ProductImgDisplayGuidelines.ProductDetails
            )
        }

        content?.invoke(productWithMultipliers)
    }
}


@Composable
fun ProductDetailsContent(
    productWithMultipliers: ProductWithMultipliers,
    modifier: Modifier = Modifier,
    viewModel: ProductDetailsViewModel
) {
    val (product, multipliers) = productWithMultipliers
    val focusManager = LocalFocusManager.current

    var inputText by remember { mutableStateOf(product.currentStock.toString()) }
    var selectedMultiplier by remember { mutableStateOf(multipliers.firstOrNull()) }
    val sliderMax = if (product.targetStock > 0) product.targetStock * 4 else 100f
    var sliderPosition by remember { mutableFloatStateOf(product.currentStock) }
    val showDialog by viewModel.productStockManager.showDialog.collectAsState()
    val predictionState by viewModel.predictionState.collectAsState()

    LaunchedEffect(product.currentStock) {
        inputText = product.currentStock.toString()
    }

    ProductDetailsTopContent(
        productWithMultipliers = productWithMultipliers,
        modifier = modifier,
        content = {
            Spacer(modifier = Modifier.height(32.dp))

            Text(text = "Quick Actions", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    if (selectedMultiplier != null) {
                        StockAdjustmentControls(
                            multipliers = multipliers,
                            currentMultiplier = selectedMultiplier!!,
                            onMultiplierSelected = { multiplier -> selectedMultiplier = multiplier },
                            onMinusClick = {
                                viewModel.productStockManager.StockAdjustmentControlsOnMinusClick(product, selectedMultiplier)
                            },
                            onPlusClick = {
                                viewModel.productStockManager.StockAdjustmentControlsOnPlusClick(product, selectedMultiplier)
                            },
                            productUnit = product.unit
                        )
                    } else {
                        ProductNoMultipliersConfiguredMessage()
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Precise Adjustment", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        label = { Text("Set stock to:") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused) {
                                    val typedValue = inputText.toFloatOrNull()
                                    if (typedValue != null && typedValue != product.currentStock) {
                                        viewModel.productStockManager.pendingDifferenceUpdate(typedValue - product.currentStock)
                                        viewModel.productStockManager.showDialogUpdate(true)
                                    } else {
                                        inputText = product.currentStock.toString()
                                    }
                                }
                            },
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Slider(
                        value = sliderPosition,
                        onValueChange = {
                            sliderPosition = it
                            val roundedVal = (it * 10f).roundToInt() / 10f
                            sliderPosition = roundedVal
                            inputText = roundedVal.toString()
                        },
                        onValueChangeFinished = {
                            if (sliderPosition != product.currentStock) {
                                val roundedVal = (sliderPosition * 10f).roundToInt() / 10f
                                viewModel.productStockManager.pendingDifferenceUpdate(roundedVal - product.currentStock)
                                viewModel.productStockManager.showDialogUpdate(true)
                            }
                        },
                        valueRange = 0f..sliderMax
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            ProductPredictionCard(
                predictionState = predictionState,
                modifier = Modifier.padding(16.dp)
            )
        }
    )

    if (showDialog) {
        SelectStockAdjustmentReasonDialog(
            onDismiss = {
                viewModel.productStockManager.StockAdjustmentDialogOnDismiss()
                inputText = product.currentStock.toString()
                sliderPosition = product.currentStock
            },
            onReasonSelected = {reason ->
                viewModel.onReasonSelected(reason)
            }
        )
    }
}