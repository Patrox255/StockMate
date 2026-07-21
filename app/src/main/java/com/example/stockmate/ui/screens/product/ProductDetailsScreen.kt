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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.stockmate.data.entity.ChangeReason
import com.example.stockmate.data.entity.Product
import com.example.stockmate.data.entity.ProductWithMultipliers
import com.example.stockmate.ui.components.LoadingIndicator
import com.example.stockmate.ui.components.form.DeleteConfirmationDialog
import com.example.stockmate.ui.components.img.ImgDisplay
import com.example.stockmate.ui.components.navigation.NavigateBack
import com.example.stockmate.ui.components.product.ProductNoMultipliersConfiguredMessage
import com.example.stockmate.ui.components.product.SelectStockAdjustmentReasonDialog
import com.example.stockmate.ui.components.product.StockAdjustmentControls
import com.example.stockmate.ui.viewmodels.ProductDetailsViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    viewModel: ProductDetailsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit
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

    Scaffold(
        // In order to make it so that when a user clicks outside the Text Field then it counts as
        // lose of the focus
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures (onTap = {
                    focusManager.clearFocus()
                })
            },
        topBar = {
            TopAppBar(
                title = { Text(details.product.name) },
                navigationIcon = {
                    NavigateBack(onNavigateBack = onNavigateBack)
                },
                actions = {
                    IconButton(onClick = {showDeleteDialog = true}) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                    }
                    IconButton(onClick = onNavigateToEdit) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->
        ProductDetailsContent(
            productWithMultipliers = details,
            modifier = Modifier.padding(paddingValues),
            onStockChange = { product, diff, reason ->
                viewModel.changeStock(product, diff, reason)
            }
        )
    }
}

@Composable
fun ProductDetailsContent(
    productWithMultipliers: ProductWithMultipliers,
    modifier: Modifier = Modifier,
    onStockChange: (Product, Float, ChangeReason) -> Unit
) {
    val (product, multipliers) = productWithMultipliers
    val focusManager = LocalFocusManager.current

    var showDialog by remember { mutableStateOf(false) }
    var pendingDifference by remember { mutableFloatStateOf(0f) }
    var inputText by remember { mutableStateOf(product.currentStock.toString()) }
    var selectedMultiplier by remember { mutableStateOf(multipliers.firstOrNull()) }

    LaunchedEffect(product.currentStock) {
        inputText = product.currentStock.toString()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
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
                contentScale = ContentScale.Fit,
                imgModifier = Modifier.fillMaxSize(),
                iconModifier = Modifier.size(64.dp),
                noImageNotificationVisible = true
            )
        }

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
                            pendingDifference = -(selectedMultiplier?.value ?: 0f)
                            showDialog = true
                        },
                        onPlusClick = {
                            pendingDifference = (selectedMultiplier?.value ?: 0f)
                            showDialog = true
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
                                    pendingDifference = typedValue - product.currentStock
                                    showDialog = true
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

                val sliderMax = if (product.targetStock > 0) product.targetStock * 4 else 100f
                var sliderPosition by remember { mutableFloatStateOf(product.currentStock) }

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
                            pendingDifference = sliderPosition - product.currentStock
                            showDialog = true
                        }
                    },
                    valueRange = 0f..sliderMax
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🧠 AI Analytics", style = MaterialTheme.typography.titleSmall)
                Text("Coming soon: predicted consumption based on history", style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    if (showDialog) {
        SelectStockAdjustmentReasonDialog(
            onDismiss = {
                showDialog = false
                inputText = product.currentStock.toString()
            },
            onReasonSelected = { reason ->
                onStockChange(product, pendingDifference, reason)
                showDialog = false
            }
        )
    }
}