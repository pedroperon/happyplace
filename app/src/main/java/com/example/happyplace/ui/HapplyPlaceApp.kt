package com.example.happyplace.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.happyplace.model.ShoppingListViewModel

enum class HappyPlaceScreen() { //val screenNameId:Int) {
    Start,//(screenNameId = R.string.app_name),
    Calendar,//(screenNameId = R.string.choose_side_dish),
    ShoppingList,//(screenNameId = R.string.choose_entree),
    Profile//(screenNameId = R.string.choose_accompaniment),
}

@Composable
fun HappyPlaceApp(
    shoppingListViewModel: ShoppingListViewModel,
    windowSize: WindowWidthSizeClass,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()

    Scaffold(
        bottomBar = {
            HappyPlaceNavigationBar()
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = HappyPlaceScreen.ShoppingList.name,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            composable(route = HappyPlaceScreen.Start.name) {
                OverviewScreen(
                    modifier = Modifier.fillMaxSize(),
                )
            }

            composable(route = HappyPlaceScreen.ShoppingList.name) {
                ShoppingListScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = shoppingListViewModel
                )
            }

            composable(route = HappyPlaceScreen.Calendar.name) {
                CalendarScreen()
            }

            composable(route = HappyPlaceScreen.Profile.name) {
                ProfileScreen()
            }
        }
    }
}

@Composable
fun HappyPlaceNavigationBar() {
    BottomAppBar {
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    tint = Color.DarkGray,
                    contentDescription = "Home screen"
                )
            }
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    tint = Color.DarkGray,
                    contentDescription = "Calendar"
                )
            }
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Filled.ShoppingCart,
                    tint = Color.DarkGray,
                    contentDescription = "Shopping list"
                )
            }
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    tint = Color.DarkGray,
                    contentDescription = "Profile"
                )
            }
        }
    }
}

//@Preview(showBackground = true, showSystemUi = true, widthDp = 700)
//@Composable
//fun HappyPlaceAppCompactPreview() {
//    HappyPlaceTheme {
//        Surface {
//            HappyPlaceApp(windowSize = WindowWidthSizeClass.Compact)
//        }
//    }
//}