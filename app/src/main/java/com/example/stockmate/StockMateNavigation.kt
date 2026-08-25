package com.example.stockmate

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.disk.DiskCache
import com.example.stockmate.ui.components.layout.MainAppLayout
import com.example.stockmate.ui.screens.chart.StockLogHistoryScreen
import com.example.stockmate.ui.screens.dish.DishDetailsScreen
import com.example.stockmate.ui.screens.dish.DishFormScreen
import com.example.stockmate.ui.screens.dish.DishInventoryScreen
import com.example.stockmate.ui.screens.inventory.InventoryScreen
import com.example.stockmate.ui.screens.product.ProductDetailsScreen
import com.example.stockmate.ui.screens.product.ProductFormScreen
import com.example.stockmate.ui.screens.settings.SettingsScreen
import com.example.stockmate.ui.viewmodels.InventoryViewModel
import com.example.stockmate.ui.viewmodels.ProductDetailsViewModel
import com.example.stockmate.ui.viewmodels.ProductFormViewModel
import com.example.stockmate.ui.viewmodels.SettingsViewModel
import com.example.stockmate.ui.viewmodels.chart.StockLogChartViewModel
import com.example.stockmate.ui.viewmodels.dish.DishDetailsViewModel
import com.example.stockmate.ui.viewmodels.dish.DishFormViewModel
import com.example.stockmate.ui.viewmodels.dish.DishInventoryViewModel

object Destinations {
    const val INVENTORY = "inventory"
    const val PRODUCT_DETAILS = "details/{productId}"
    const val ADD_PRODUCT = "add-product"
    const val EDIT_PRODUCT = "edit-product/{productId}"
    const val SETTINGS = "settings"
    const val STOCK_LOG_CHART = "stock-log-chart"
    const val ADD_DISH = "add-dish"
    const val EDIT_DISH = "edit-dish/{dishId}"
    const val DISH_INVENTORY = "dish-inventory"
    const val DISH_DETAILS = "dish-details/{dishId}"
}

fun destinationToHeader(destination: String?): String {
    return when (destination) {
        Destinations.SETTINGS -> "App Settings"
        Destinations.ADD_PRODUCT -> "Add Product"
        Destinations.EDIT_PRODUCT -> "Edit Product"
        Destinations.PRODUCT_DETAILS -> "Product Details"
        Destinations.STOCK_LOG_CHART -> "Stock Log History"
        Destinations.ADD_DISH -> "Add Dish"
        Destinations.EDIT_DISH -> "Edit Dish"
        Destinations.DISH_INVENTORY -> "Dishes"
        Destinations.DISH_DETAILS -> "Dish Details"
        else -> "StockMate"
    }
}

@Composable
fun StockMateNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var dynamicTitle by remember { mutableStateOf<String?>(null) }
    var dynamicActions by remember { mutableStateOf<(@Composable RowScope.()->Unit)?>(null) }
    var dynamicNavigationAction by remember { mutableStateOf<(@Composable ()->Unit)?>(null) }

    LaunchedEffect(currentRoute) {
        dynamicTitle = null
        dynamicNavigationAction = when (currentRoute) {
            Destinations.INVENTORY -> {
                {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = "Logo",
                        modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            else -> null
        }
        dynamicActions = when (currentRoute) {
            Destinations.INVENTORY -> {
                {
                    IconButton(onClick = { navController.navigate(Destinations.STOCK_LOG_CHART) }) {
                        Icon(Icons.Default.BarChart, contentDescription = "Analytics")
                    }
                    IconButton(onClick = {
                        navController.navigate(Destinations.SETTINGS)
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = {
                        navController.navigate(Destinations.DISH_INVENTORY)
                    }) {
                        Icon(Icons.Default.RestaurantMenu, contentDescription = "Dishes")
                    }
                }
            }
            else -> null
        }

    }
    val onTitleUpdate: (String?) -> Unit = { newTitle: String? ->
        dynamicTitle = newTitle
    }
    val onActionsUpdate: ((@Composable RowScope.() -> Unit)?) -> Unit = { newActions ->
        dynamicActions = newActions
    }
    val onNavigationActionUpdate: ((@Composable (() -> Unit))?) -> Unit = { newNavigationAction ->
        dynamicNavigationAction = newNavigationAction
    }
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    MainAppLayout(
        onNavigateBack = {
            backDispatcher?.onBackPressed()
        },
        currentRoute = currentRoute,
        dynamicTitle = dynamicTitle,
        dynamicActions = dynamicActions,
        dynamicNavigationIcon = dynamicNavigationAction,
        content = { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Destinations.INVENTORY,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Destinations.INVENTORY) {
                    val viewModel: InventoryViewModel = hiltViewModel()

                    InventoryScreen(
                        viewModel = viewModel,
                        onNavigateToDetails = { productId ->
                            navController.navigate(Destinations.PRODUCT_DETAILS.replace("{productId}", productId.toString()))
                        },
                        onNavigateToAddProduct = {
                            navController.navigate(Destinations.ADD_PRODUCT)
                        }
                    )
                }

                composable(
                    route = Destinations.PRODUCT_DETAILS,
                    arguments = listOf(navArgument("productId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val viewModel: ProductDetailsViewModel = hiltViewModel()
                    val productId = backStackEntry.arguments!!.getLong("productId")

                    ProductDetailsScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToEdit = {
                            navController.navigate(Destinations.EDIT_PRODUCT.replace("{productId}", productId.toString()))
                        },
                        onTitleUpdate = onTitleUpdate,
                        onActionsUpdate = onActionsUpdate
                    )
                }

                composable(
                    route = Destinations.ADD_PRODUCT
                ) {
                    val viewModel: ProductFormViewModel = hiltViewModel()

                    ProductFormScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    route = Destinations.EDIT_PRODUCT,
                    arguments = listOf(navArgument("productId") { type = NavType.LongType })
                ) {
                    val viewModel: ProductFormViewModel = hiltViewModel()

                    ProductFormScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    route = Destinations.SETTINGS
                ) {
                    val viewModel: SettingsViewModel = hiltViewModel()

                    SettingsScreen(
                        viewModel = viewModel
                    )
                }

                composable(
                    route = Destinations.STOCK_LOG_CHART
                ) {
                    val viewModel: StockLogChartViewModel = hiltViewModel()

                    StockLogHistoryScreen(
                        viewModel = viewModel
                    )
                }

                composable(
                    route = Destinations.ADD_DISH
                ) {
                    val viewModel: DishFormViewModel = hiltViewModel()

                    DishFormScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    route = Destinations.EDIT_DISH,
                    arguments = listOf(navArgument("dishId") { type = NavType.LongType })
                ) {
                    val viewModel: DishFormViewModel = hiltViewModel()

                    DishFormScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    route = Destinations.DISH_INVENTORY
                ) {
                    val viewModel: DishInventoryViewModel = hiltViewModel()

                    DishInventoryScreen(
                        viewModel = viewModel,
                        onDishClick = { dishId ->
                            navController.navigate(Destinations.DISH_DETAILS.replace("{dishId}", dishId.toString()))
                        },
                        onNewDishClick = {
                            navController.navigate(Destinations.ADD_DISH)
                        }
                    )
                }

                composable(
                    route = Destinations.DISH_DETAILS,
                    arguments = listOf(navArgument("dishId") { type = NavType.LongType })
                ) {
                    val viewModel: DishDetailsViewModel = hiltViewModel()

                    DishDetailsScreen(
                        viewModel = viewModel,
                        onEditDishClick = { dishId ->
                            navController.navigate(Destinations.EDIT_DISH.replace("{dishId}", dishId.toString()))
                        }
                    )
                }
            }
        }
    )
}