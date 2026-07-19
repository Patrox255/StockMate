package com.example.stockmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.stockmate.data.repository.ProductRepository
import com.example.stockmate.ui.screens.inventory.InventoryScreen
import com.example.stockmate.ui.screens.product.ProductFormScreen
import com.example.stockmate.ui.screens.product.ProductDetailsScreen
import com.example.stockmate.ui.viewmodels.ProductFormViewModel
import com.example.stockmate.ui.viewmodels.InventoryViewModel
import com.example.stockmate.ui.viewmodels.ProductDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var repository: ProductRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repository.seedSampleData()
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "inventory") {
                        composable("inventory") {
                            val viewModel: InventoryViewModel = hiltViewModel()

                            InventoryScreen(
                                viewModel = viewModel,
                                onNavigateToDetails = {productId ->
                                    navController.navigate("details/${productId}")
                                },
                                onNavigateToAddProduct = {
                                    navController.navigate("add-product")
                                }
                            )
                        }

                        composable(
                            route = "details/{productId}",
                            arguments = listOf(navArgument("productId") {type = NavType.LongType})
                        ) { backStackEntry ->
                            val viewModel: ProductDetailsViewModel = hiltViewModel()
                            val productId = backStackEntry.arguments!!.getLong("productId")

                            ProductDetailsScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToEdit = {
                                    navController.navigate("edit-product/${productId}")
                                }
                            )
                        }

                        composable(
                            route = "add-product"
                        ) {
                           val viewModel : ProductFormViewModel = hiltViewModel()

                            ProductFormScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable (
                            route = "edit-product/{productId}",
                            arguments = listOf(navArgument("productId") {type = NavType.LongType})
                        ) {
                            val viewModel : ProductFormViewModel = hiltViewModel()

                            ProductFormScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                    }
                }
            }
        }
    }
}