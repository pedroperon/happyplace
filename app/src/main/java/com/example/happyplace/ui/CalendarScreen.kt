package com.example.happyplace.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.PlayArrow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.happyplace.R
import com.example.happyplace.Task
import com.example.happyplace.model.EditTaskViewModel
import com.example.happyplace.model.TasksCalendarViewModel
import com.example.happyplace.utils.firstSundayAfterCurrentMonth
import com.example.happyplace.utils.lastMondayBeforeCurrentMonth
import com.example.happyplace.utils.startOfDayMillis
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.round

@Composable
fun CalendarScreen(
    tasksCalendarViewModel: TasksCalendarViewModel,
    editTaskViewModel: EditTaskViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by tasksCalendarViewModel.uiState.collectAsState()

    BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
        ) {
        val boxWithConstraintsScope = this
        HorizontalPager(state = rememberPagerState(pageCount = { 12000 })) { monthOffset ->
            val tasks = tasksCalendarViewModel.getTasksForMonth(monthOffset)
            Column {
                DaysOfTheWeekInitialsHeader()
                MonthBox(
                    monthOffset = monthOffset,
                    onClickDay = { tasksCalendarViewModel.toggleShowDay(it) },
                    expandedDay = uiState.expandedDay,
                    tasksInMonth = tasks,
                    modifier = Modifier.width(boxWithConstraintsScope.maxWidth)
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
    tasksInMonth: List<Task>,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val firstDayOfMonth = today.withDayOfMonth(1).plusMonths(monthOffset.toLong())

    val startDay = firstDayOfMonth.lastMondayBeforeCurrentMonth()
    val endDay = firstDayOfMonth.firstSundayAfterCurrentMonth()

    Box(modifier = modifier
        .fillMaxHeight()
        .background(Color.White)
        .verticalScroll(rememberScrollState(0))
    ) {
        Text(text = stringResource(MONTHS_NAME_IDS[firstDayOfMonth.monthValue-1]),
            fontSize = 50.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFEEEEEE),
            modifier = Modifier.padding(start = 16.dp))

        val monthLength = firstDayOfMonth.lengthOfMonth()
        val dayRowsTotal = (round((endDay.toEpochDay() - startDay.toEpochDay() + 1)/7.0)).toInt()

        var dayRowsIncludingExpandedDay = dayRowsTotal
        if(expandedDay!=null &&
            (expandedDay-firstDayOfMonth.toEpochDay()) in (0..monthLength)) {
            dayRowsIncludingExpandedDay =
                (ceil((expandedDay-startDay.toEpochDay()).toDouble()/7.0)).toInt()
        }
        val dayRowsAfterExpandedDay = dayRowsTotal - dayRowsIncludingExpandedDay

        Column {
            repeat(dayRowsIncludingExpandedDay) { weekCount ->
                Row {
                    repeat(7) { dayCount ->
                        val day = startDay.plusDays((weekCount*7+dayCount).toLong())
                        DayBox(
                            day = day,
                            onClick = onClickDay,
                            isExpanded = (day.toEpochDay() == expandedDay),
                            isInMonth = (firstDayOfMonth==day.withDayOfMonth(1)),
                            isToday = (day == today),
                            tasks = tasksInMonth.filter { it.initialDate == day.startOfDayMillis() }
                        )
                    }
                }
            }
            DayTasksBox(expandedDay, tasksInMonth)
            repeat(dayRowsAfterExpandedDay) { weekCount ->
                Row {
                    repeat(7) { dayCount ->
                        val day = startDay.plusDays(((dayRowsIncludingExpandedDay+weekCount)*7+dayCount).toLong())
                        DayBox(
                            day = day,
                            onClick = onClickDay,
                            isExpanded = (day.toEpochDay() == expandedDay),
                            isToday = (day == today),
                            tasks = tasksInMonth.filter { it.initialDate == day.startOfDayMillis() },
                            isInMonth = (firstDayOfMonth==day.withDayOfMonth(1))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DayTasksBox(epochDay:Long?, tasks: List<Task>) {

    Column(modifier = Modifier
        .fillMaxWidth()
        .background(Color(0x40005500))
        .animateContentSize()) {
        if (epochDay != null && tasks.isNotEmpty()) {
            val dayTasks = tasks.filter {
                it.initialDate == LocalDate.ofEpochDay(epochDay).startOfDayMillis()
            }

            if (dayTasks.isNotEmpty()) {
                Column(modifier = Modifier.padding(horizontal=16.dp, vertical = 8.dp)) {
                    Text(
                        text = stringResource(R.string.tasks_of_the_day,
                            LocalDate
                                .ofEpochDay(epochDay)
                                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
                        ),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    for (task in dayTasks) {
                        TaskBox(task)
                    }
                }
            }
        }
    }
}
@Composable
fun TaskBox(task:Task) {
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable {  }
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ) {
        val color = Color.DarkGray //TODO: make this dependent on taks type
        Icon(Icons.Rounded.PlayArrow, null, tint = color, modifier = Modifier.size(12.dp))
        Text(text = task.name,
            fontWeight = FontWeight.W400,
            color = color,
            modifier = Modifier.padding(horizontal = 4.dp))
    }
}

@Composable
private fun RowScope.DayBox (
    day: LocalDate,
    onClick: (Long) -> Unit,
    isExpanded: Boolean,
    isToday: Boolean,
    tasks: List<Task>,
    isInMonth: Boolean
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .aspectRatio(0.9F)
            .weight(1F)
            .clickable { onClick(day.toEpochDay()) }
            .background(if (isExpanded) Color(0x40005500) else Color.Transparent)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${day.dayOfMonth}",
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = if(isInMonth) Color.Black else Color.LightGray
            )
            var dots = ""
            for (task in tasks) {
                // 1 dot for each task in this day
                dots += "·"
            }
            Text(text = dots, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun DaysOfTheWeekInitialsHeader() {
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(32.dp)) {
        for (index in 0..<DayOfWeek.entries.size) {
            val dayName = stringResource(DAYS_OF_WEEK_NAME_IDS[index])
            Text(
                text = dayName.substring(0, min(3, dayName.length)),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1F)
            )
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