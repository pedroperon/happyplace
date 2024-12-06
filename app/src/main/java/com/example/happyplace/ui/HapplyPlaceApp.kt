package com.example.happyplace.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.happyplace.R
import com.example.happyplace.model.ShoppingListViewModel
import com.example.happyplace.ui.theme.HappyPlaceTheme

@Composable
fun HappyPlaceApp(
    shoppingListViewModel: ShoppingListViewModel,
    windowSize: WindowWidthSizeClass,
    modifier: Modifier = Modifier) {

//    val shoppingListViewModel: ShoppingListViewModel = viewModel()

    Scaffold(
        topBar = {
            HappyPlaceAppBar()
        },
        floatingActionButton = { AddItemFloatingActionButton(onClick = { shoppingListViewModel.openNewItemDialog() }) }
    ) { innerPadding ->

        ShoppingList(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding,
            viewModel = shoppingListViewModel
        )
    }
}

@Composable
fun AddItemFloatingActionButton(onClick : ()->Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        containerColor = Color.Gray,
        contentColor = Color.White,
        icon = { Icon(Icons.Filled.Add, "Extended floating action button.") },
        text = { Text(text = stringResource(R.string.add_item)) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HappyPlaceAppBar() {
    TopAppBar(
        title = {
            Text(text = stringResource(R.string.app_name), fontWeight = FontWeight.SemiBold)
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.LightGray, titleContentColor = Color.White)
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