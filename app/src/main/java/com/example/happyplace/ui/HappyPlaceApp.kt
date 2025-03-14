package com.example.happyplace.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.rounded.Add
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.happyplace.R
import com.example.happyplace.model.ShoppingListViewModel
import com.example.happyplace.model.TasksCalendarViewModel

enum class HappyPlaceScreen(val screenNameId:Int) {
    Start(screenNameId = R.string.app_name),
    Calendar(screenNameId = R.string.calendar),
    ShoppingList(screenNameId = R.string.shopping_list),
    Profile(screenNameId = R.string.parameters),
}

@Composable
fun HappyPlaceApp(
    shoppingListViewModel: ShoppingListViewModel,
    tasksCalendarViewModel: TasksCalendarViewModel,
    windowSize: WindowWidthSizeClass,
    modifier: Modifier = Modifier,
) {
    var currentScreen by rememberSaveable { mutableStateOf(HappyPlaceScreen.Start) }

    val context = LocalContext.current

    var hasNotificationPermission by rememberSaveable {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { hasNotificationPermission = it }
    )

    Scaffold(
        bottomBar = {
            HappyPlaceNavigationBar(currentScreen = currentScreen, onSelectTab = { currentScreen=it })
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
                        buttonTitleId = R.string.new_task,
                        onClick = {
                            tasksCalendarViewModel.openNewTaskDialog()

                            if (!hasNotificationPermission) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        }
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
                    OverviewScreen(
                        shoppingListViewModel = shoppingListViewModel,
                        tasksCalendarViewModel = tasksCalendarViewModel,
                        onCallCalendarScreen = { currentScreen = HappyPlaceScreen.Calendar },
                        onCallShoppingListScreen = {currentScreen = HappyPlaceScreen.ShoppingList},
                    )

                HappyPlaceScreen.ShoppingList ->
                    ShoppingListScreen(
                        shoppingListViewModel = shoppingListViewModel
                    )

                HappyPlaceScreen.Calendar ->
                    CalendarScreen(
                        tasksCalendarViewModel = tasksCalendarViewModel,
                    )

                HappyPlaceScreen.Profile ->
                    ProfileScreen(
                        tasksCalendarViewModel = tasksCalendarViewModel,
                    )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HappyPlaceTopBar(currentScreen: HappyPlaceScreen) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(currentScreen.screenNameId),
                fontWeight = FontWeight.SemiBold
            )
        },
        colors = TopAppBarDefaults
            .topAppBarColors(
                containerColor = Color(0xFF005500),
                titleContentColor = Color.White),
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
                    selected = currentScreen == HappyPlaceScreen.Start,
                    onSelectTab = { onSelectTab(HappyPlaceScreen.Start) },
                    modifier = Modifier.weight(1f)
                )
                BottomBarButton(
                    screenNameId = HappyPlaceScreen.Start.screenNameId,
                    imageVector = Icons.Filled.DateRange,
                    selected = currentScreen == HappyPlaceScreen.Calendar,
                    onSelectTab = { onSelectTab(HappyPlaceScreen.Calendar) },
                    modifier = Modifier.weight(1f)
                )
                BottomBarButton(
                    screenNameId = HappyPlaceScreen.Start.screenNameId,
                    imageVector = Icons.Filled.ShoppingCart,
                    selected = currentScreen == HappyPlaceScreen.ShoppingList,
                    onSelectTab = { onSelectTab(HappyPlaceScreen.ShoppingList) },
                    modifier = Modifier.weight(1f)
                )
                BottomBarButton(
                    screenNameId = HappyPlaceScreen.Start.screenNameId,
                    imageVector = Icons.Filled.AccountCircle,
                    selected = currentScreen == HappyPlaceScreen.Profile,
                    onSelectTab = { onSelectTab(HappyPlaceScreen.Profile) },
                    modifier = Modifier.weight(1f)
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
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onSelectTab,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) Color.LightGray else Color.Transparent)
    ) {
        Icon(
            imageVector = imageVector,
            tint = if(selected) Color(0xFF005500) else Color.Gray,
            contentDescription = stringResource(screenNameId),
        )
    }
}

@Composable
fun AddItemFloatingActionButton(onClick: () -> Unit, buttonTitleId: Int) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        containerColor = Color(0xFF005500),
        contentColor = Color.White,
        icon = { Icon(Icons.Rounded.Add, "Extended floating action button.", Modifier.size(32.dp)) },
        text = { Text(
            text = stringResource(buttonTitleId),
            fontWeight = FontWeight.SemiBold,
            //fontSize = 20.dp
        ) },
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