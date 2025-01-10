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
import com.example.happyplace.data.TasksListRepository
import com.example.happyplace.data.TasksListSerializer
import com.example.happyplace.model.ShoppingListViewModel
import com.example.happyplace.model.ShoppingListViewModelFactory
import com.example.happyplace.model.TasksCalendarViewModel
import com.example.happyplace.model.TasksCalendarViewModelFactory
import com.example.happyplace.ui.HappyPlaceApp
import com.example.happyplace.ui.theme.HappyPlaceTheme

private const val SHOPPING_LIST_DATA_STORE_FILE_NAME = "shopping_list.pb"
private const val TASKS_DATA_STORE_FILE_NAME = "tasks_list.pb"

// Build the DataStores
private val Context.shoppingListStore: DataStore<LocalShoppingList> by dataStore(
    fileName = SHOPPING_LIST_DATA_STORE_FILE_NAME,
    serializer = ShoppingListSerializer
)
private val Context.tasksStore: DataStore<LocalTasksList> by dataStore(
    fileName = TASKS_DATA_STORE_FILE_NAME,
    serializer = TasksListSerializer
)

class MainActivity : ComponentActivity() {

    private lateinit var shoppingListViewModel: ShoppingListViewModel
    private lateinit var tasksCalendarViewModel: TasksCalendarViewModel

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Shopping list initialization
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

        // Tasks calendar initialization
        tasksCalendarViewModel =
            ViewModelProvider(
                this,
                TasksCalendarViewModelFactory(
                    TasksListRepository(tasksStore)
                )
            )[TasksCalendarViewModel::class.java]

        tasksCalendarViewModel.initialSetupEvent.observe(this) { initialSetupEvent ->
            tasksCalendarViewModel.setTasksList(initialSetupEvent)
            observeTasksListChanges()
        }


        enableEdgeToEdge()
        setContent {
            HappyPlaceTheme {
                Surface {
                    val windowSize = calculateWindowSizeClass(this)
                    HappyPlaceApp(
                        shoppingListViewModel,
                        tasksCalendarViewModel,
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
    private fun observeTasksListChanges() {
        tasksCalendarViewModel.tasksListUiState.observe(this) { retrievedTasksListUiModel ->
            tasksCalendarViewModel.setTasksList(retrievedTasksListUiModel)
        }
    }
}
