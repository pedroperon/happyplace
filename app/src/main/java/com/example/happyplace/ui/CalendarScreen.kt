package com.example.happyplace.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.happyplace.R
import com.example.happyplace.Task
import com.example.happyplace.model.EditTaskViewModel
import com.example.happyplace.model.TasksCalendarViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.min

@Composable
fun CalendarScreen(
    tasksCalendarViewModel: TasksCalendarViewModel,
    editTaskViewModel: EditTaskViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by tasksCalendarViewModel.uiState.collectAsState()

    Column {
        DaysOfTheWeekInitials()
        Column(
            modifier = modifier.fillMaxSize()//.verticalScroll(rememberScrollState(0))
        ) {
            for(monthOffset in 0..5) {
                val tasks = tasksCalendarViewModel.getTasksForMonth(monthOffset)
                MonthBox(
                    monthOffset = monthOffset,
                    onClickDay = { tasksCalendarViewModel.toggleShowDay(it) },
                    expandedDay = uiState.expandedDay,
                    tasks = tasks
                )
            }
        }
    }

    if(uiState.showEditTaskDialog) {
        EditTaskPopupDialog(
            initialEpochDay = uiState.expandedDay,
            onDismissRequest = {
                tasksCalendarViewModel.closeEditTaskDialog()
                editTaskViewModel.setTaskBeingEdited(null)
                               },
            onClickSave = { tasksCalendarViewModel.saveTask(it) },
            editTaskViewModel = editTaskViewModel
        )
    }

}

@Composable
fun MonthBox(
    monthOffset: Int,
    onClickDay: (Long) -> Unit,
    expandedDay: Long?,
    tasks: List<Task>
) {
    val today = LocalDate.now()
    val startDay = today.withDayOfMonth(1).plusMonths(monthOffset.toLong())

    Box(Modifier.background(if(monthOffset%2==1) Color(0xFFF4F4F4) else Color.White)) {
        Text(text = stringResource(MONTHS_NAME_IDS[startDay.monthValue-1]),
            fontSize = 50.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFEEEEEE),
            modifier = Modifier.padding(start = 16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(7)
        ) {
            items(startDay.dayOfWeek.ordinal) {
                // for aligning with days of week on header
            }
            items(startDay.lengthOfMonth()) { count ->
                val day = startDay.plusDays(count.toLong())
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .aspectRatio(1F)
                        .fillMaxSize()
                        .clickable { onClickDay(day.toEpochDay()) }
                        .background(if(day.toEpochDay()==expandedDay) Color(0x40005500) else Color.Transparent)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${day.dayOfMonth}",
                            fontWeight = if (day.toEpochDay() == today.toEpochDay()) FontWeight.Bold else FontWeight.Normal
                        )
                        var dots = ""
                            for(task in tasks.filter{
                                it.initialDate == day.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
                            }) {
                                // 1 dot for each task in this day
                                dots += "·"
                            }
                        Text(text = dots)
                    }
                }
            }
        }
    }
}

@Composable
fun DaysOfTheWeekInitials() {
    Row(horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        for(index in 0..<DayOfWeek.entries.size) {
            val dayName = stringResource(DAYS_OF_WEEK_NAME_IDS[index])
            Text(text = dayName.substring(0,min(3,dayName.length)),
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.SemiBold)
        }
    }
}

private val DAYS_OF_WEEK_NAME_IDS = arrayOf(
    R.string.monday,
    R.string.tuesday,
    R.string.wednesday,
    R.string.thursday,
    R.string.friday,
    R.string.saturday,
    R.string.sunday
)

private val MONTHS_NAME_IDS = arrayOf(
    R.string.january,
    R.string.february,
    R.string.march,
    R.string.april,
    R.string.may,
    R.string.june,
    R.string.july,
    R.string.august,
    R.string.september,
    R.string.october,
    R.string.november,
    R.string.december
)