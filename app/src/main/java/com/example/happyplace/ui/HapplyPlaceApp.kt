package com.example.happyplace.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.happyplace.R
import com.example.happyplace.model.ShoppingListViewModel
import com.example.happyplace.model.TasksCalendarViewModel

enum class HappyPlaceScreen(val screenNameId:Int) {
    Start(screenNameId = R.string.app_name),
    Calendar(screenNameId = R.string.calendar),
    ShoppingList(screenNameId = R.string.shopping_list),
    Profile(screenNameId = R.string.parameters),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HappyPlaceApp(
    shoppingListViewModel: ShoppingListViewModel,
    tasksCalendarViewModel: TasksCalendarViewModel,
    windowSize: WindowWidthSizeClass,
    modifier: Modifier = Modifier,
) {
    var currentScreen by rememberSaveable { mutableStateOf(HappyPlaceScreen.ShoppingList) }

    Scaffold(
        bottomBar = {
            HappyPlaceNavigationBar(currentScreen, { currentScreen = it })
        },
        topBar = {
            HappyPlaceTopBar(currentScreen)
        },
        floatingActionButton = {
            when(currentScreen) {
                HappyPlaceScreen.ShoppingList -> {
                    AddItemFloatingActionButton(
                        buttonTitleId = R.string.add_item,
                        onClick = { shoppingListViewModel.openNewItemDialog() }
                    )
                }
                HappyPlaceScreen.Calendar -> {
                    AddItemFloatingActionButton(
                        buttonTitleId = R.string.add_task,
                        onClick = { tasksCalendarViewModel.openNewTaskDialog() }
                    )
                }
                else -> {}
            }
        },
    ) { innerPadding ->

        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
            when (currentScreen) {
                HappyPlaceScreen.Start ->
                    OverviewScreen()

                HappyPlaceScreen.ShoppingList ->
                    ShoppingListScreen(
                        shoppingListViewModel = shoppingListViewModel
                    )

                HappyPlaceScreen.Calendar ->
                    CalendarScreen()

                HappyPlaceScreen.Profile ->
                    ProfileScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HappyPlaceTopBar(currentScreen: HappyPlaceScreen) {
    TopAppBar(
        title = {
            Column() {
//                Text(
//                    text = stringResource(R.string.app_name),
//                    fontWeight = FontWeight.SemiBold,
//                    fontSize = 12.sp
//                )
                Text(
                    text = stringResource(currentScreen.screenNameId),
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF005500), titleContentColor = Color.White),
    )
}

@Composable
fun HappyPlaceNavigationBar(
    currentScreen: HappyPlaceScreen,
    onSelectTab:(HappyPlaceScreen)->Unit
) {
    BottomAppBar(
        actions = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                BottomBarButton(
                    screenNameId = HappyPlaceScreen.Start.screenNameId,
                    imageVector = Icons.Filled.Home,
                    selected = currentScreen==HappyPlaceScreen.Start,
                    onSelectTab = { onSelectTab(HappyPlaceScreen.Start) }
                )
                BottomBarButton(
                    screenNameId = HappyPlaceScreen.Start.screenNameId,
                    imageVector = Icons.Filled.DateRange,
                    selected = currentScreen==HappyPlaceScreen.Calendar,
                    onSelectTab = { onSelectTab(HappyPlaceScreen.Calendar) }
                )
                BottomBarButton(
                    screenNameId = HappyPlaceScreen.Start.screenNameId,
                    imageVector = Icons.Filled.ShoppingCart,
                    selected = currentScreen==HappyPlaceScreen.ShoppingList,
                    onSelectTab = { onSelectTab(HappyPlaceScreen.ShoppingList) }
                )
                BottomBarButton(
                    screenNameId = HappyPlaceScreen.Start.screenNameId,
                    imageVector = Icons.Filled.AccountCircle,
                    selected = currentScreen==HappyPlaceScreen.Profile,
                    onSelectTab = { onSelectTab(HappyPlaceScreen.Profile) }
                )
            }
        }
    )
}

@Composable
private fun BottomBarButton(
    screenNameId: Int,
    imageVector: ImageVector,
    onSelectTab: () -> Unit,
    selected: Boolean
) {
    IconButton(
        onClick = onSelectTab
    ) {
        Icon(
            imageVector = imageVector,
            tint = if(selected) Color.DarkGray else Color.Gray,
            contentDescription = stringResource(screenNameId)
        )
    }
}

@Composable
fun AddItemFloatingActionButton(onClick: () -> Unit, buttonTitleId: Int) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        containerColor = Color.Gray,
        contentColor = Color.White,
        icon = { Icon(Icons.Filled.Add, "Extended floating action button.") },
        text = { Text(text = stringResource(buttonTitleId)) },
    )
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