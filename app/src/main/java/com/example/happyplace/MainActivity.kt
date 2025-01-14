package com.example.happyplace

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.lifecycle.ViewModelProvider
import com.example.happyplace.data.ShoppingListRepository
import com.example.happyplace.data.ShoppingListSerializer
import com.example.happyplace.model.ShoppingListViewModel
import com.example.happyplace.model.ShoppingListViewModelFactory
import com.example.happyplace.ui.HappyPlaceApp
import com.example.happyplace.ui.theme.HappyPlaceTheme

private const val DATA_STORE_FILE_NAME = "shopping_list.pb"

// Build the DataStore
private val Context.shoppingListStore: DataStore<LocalShoppingList> by dataStore(
    fileName = DATA_STORE_FILE_NAME,
    serializer = ShoppingListSerializer
)

class MainActivity : ComponentActivity() {

    private lateinit var shoppingListViewModel: ShoppingListViewModel

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        shoppingListViewModel =
            ViewModelProvider(
            this,
            ShoppingListViewModelFactory(
                ShoppingListRepository(shoppingListStore)
            )
        )[ShoppingListViewModel::class.java]

        shoppingListViewModel.initialSetupEvent.observe(this) { initialSetupEvent ->
            shoppingListViewModel.setShoppingList(initialSetupEvent)
            observeShoppingListChanges()
        }
        enableEdgeToEdge()
        setContent {
            HappyPlaceTheme {
                Surface {
                    val windowSize = calculateWindowSizeClass(this)
                    HappyPlaceApp(
                        shoppingListViewModel,
                        windowSize.widthSizeClass)
                }
            }
        }
    }

    private fun observeShoppingListChanges() {
        shoppingListViewModel.shoppingListUiState.observe(this) { retrievedShoppingListUiModel ->
            shoppingListViewModel.setShoppingList(retrievedShoppingListUiModel)
        }
    }
}
