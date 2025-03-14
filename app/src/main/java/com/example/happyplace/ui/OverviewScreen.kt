package com.example.happyplace.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.happyplace.R
import com.example.happyplace.model.ShoppingListViewModel
import com.example.happyplace.model.TasksCalendarViewModel
import com.example.happyplace.utils.containsDateTimeInMillis
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun OverviewScreen(
    shoppingListViewModel: ShoppingListViewModel,
    tasksCalendarViewModel: TasksCalendarViewModel,
    onCallCalendarScreen: () -> Unit,
    onCallShoppingListScreen: () -> Unit,
    modifier: Modifier = Modifier,
    ) {
    val shoppingListUiState by shoppingListViewModel.uiState.collectAsState()
    val tasksUiState by tasksCalendarViewModel.uiState.collectAsState()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(8.dp)
    ) {
        Text(text = stringResource(R.string.hello, "Pedro"))
        Text(text = stringResource(
            R.string.it_s,
            LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
        ))

        val urgentItems = shoppingListUiState.shoppingList.filter { it.urgent && !it.isInCart }
        if(urgentItems.isNotEmpty()) {
            Column {
                Text(text = stringResource(R.string.there_are_some_urgent_items_on_your_shopping_list))
                for(item in urgentItems) {
                    Text(text = " - ${item.name}")
                }
            }
        }

        val todayTasks = tasksUiState.tasks.filter{ LocalDate.now().containsDateTimeInMillis(it.initialDate) }
        if(todayTasks.isEmpty()) {
            Text(text = stringResource(R.string.relax_you_have_no_tasks_due_today))
        }
        else {
            Column {
                Text(text = stringResource(R.string.you_have_some_tasks_today))
                for(task in todayTasks.sortedBy { it.initialDate }) {
                    TaskBox(task = task, onClick = {
                        onCallCalendarScreen()
                        tasksCalendarViewModel.toggleShowDay(
//                            Instant.ofEpochMilli(it.initialDate).atZone(ZoneId.systemDefault()).
                            LocalDate.ofInstant(Instant.ofEpochMilli(it.initialDate), ZoneId.systemDefault()).toEpochDay(),
                            allowUnselect = false
                        )
                    })
                }
            }
        }
    }
}