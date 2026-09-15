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
import com.example.compose.AppTheme
import com.example.stockmate.data.repository.ProductRepository
import com.example.stockmate.data.seeder.DatabaseSeeder
import com.example.stockmate.data.seeder.SeederAction
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
    lateinit var databaseSeeder: DatabaseSeeder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            databaseSeeder.seedDatabase(
                // DEVELOPMENT ONLY
                seederAction = SeederAction.SEED
            )
        }

        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StockMateNavigation()
                }
            }
        }
    }
}