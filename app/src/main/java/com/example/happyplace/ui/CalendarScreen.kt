package com.example.happyplace.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.round

@Composable
fun CalendarScreen(
    tasksCalendarViewModel: TasksCalendarViewModel,
    editTaskViewModel: EditTaskViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by tasksCalendarViewModel.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        HorizontalPager(state = rememberPagerState(pageCount = { 12000 })) { monthOffset ->
            val tasks = tasksCalendarViewModel.getTasksForMonth(monthOffset)
            Column {
                DaysOfTheWeekInitialsHeader()
                Box(modifier = modifier
                    .fillMaxHeight()
                    .background(Color.White)
                    .clickable { tasksCalendarViewModel.toggleShowDay(null) }
                    .verticalScroll(rememberScrollState(0))
                ) {
                    MonthBox(
                        monthOffset = monthOffset,
                        onClickDay = { tasksCalendarViewModel.toggleShowDay(it) },
                        expandedDay = uiState.expandedDay,
                        tasksInMonth = tasks,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
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
    onClickDay: (Long?) -> Unit,
    expandedDay: Long?,
    tasksInMonth: List<Task>,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val firstDayOfMonth = today.withDayOfMonth(1).plusMonths(monthOffset.toLong())

    val startDay = firstDayOfMonth.lastMondayBeforeCurrentMonth()
    val endDay = firstDayOfMonth.firstSundayAfterCurrentMonth()
    val dayRowsTotal = (round((endDay.toEpochDay() - startDay.toEpochDay() + 1) / 7.0)).toInt()

    Box(Modifier.wrapContentSize()) {
        Text(
            text = firstDayOfMonth.month.getDisplayName(TextStyle.FULL,Locale.getDefault()),
            fontSize = 50.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFEEEEEE),
            modifier = Modifier.padding(16.dp).fillMaxSize()
        )

        Column {
            repeat(dayRowsTotal) { weekCount ->
                WeekBox(
                    startDay.plusWeeks(weekCount.toLong()),
                    onClickDay,
                    expandedDay,
                    firstDayOfMonth,
                    today,
                    tasksInMonth
                )
            }
        }
    }
}

@Composable
private fun WeekBox(
    startDay: LocalDate,
    onClickDay: (Long) -> Unit,
    expandedDay: Long?,
    firstDayOfMonth: LocalDate?,
    today: LocalDate?,
    tasksInMonth: List<Task>
) {
    Row {
        repeat(7) { dayCount ->
            val day = startDay.plusDays(dayCount.toLong())
            val isInMonth = (day.withDayOfMonth(1)==firstDayOfMonth)
            DayBox(
                day = day,
                onClick = {
                    onClickDay(it)
                    if(!isInMonth) {}
                          },
                isExpanded = (day.toEpochDay() == expandedDay),
                isToday = (day == today),
                tasks = tasksInMonth.filter { it.initialDate == day.startOfDayMillis() },
                isInMonth = (firstDayOfMonth == day.withDayOfMonth(1)),
                modifier = Modifier.weight(1F)
            )
        }
    }
    DayTasksBox(
        epochDay = expandedDay,
        weekStart = startDay,
        tasks = tasksInMonth
    )
}

@Composable
fun DayTasksBox(epochDay: Long?, weekStart: LocalDate, tasks: List<Task>) {

    Column(modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
        .background(Color(0x40005500))
        .animateContentSize()) {
        if (epochDay != null &&
            (epochDay - weekStart.toEpochDay()) in 0..<7
        ) {
            val dayTasks = tasks.filter {
                it.initialDate == LocalDate.ofEpochDay(epochDay).startOfDayMillis()
            }

            if (dayTasks.isNotEmpty()) {
                Column(modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(
                            R.string.tasks_of_the_day,
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
        Icon(painterResource(R.drawable.baseline_arrow_right_24), null, tint = color)
        Text(text = task.name,
            //fontWeight = FontWeight.W400,
            color = color,
            modifier = Modifier.padding(horizontal = 4.dp))
    }
}

@Composable
private fun DayBox (
    day: LocalDate,
    onClick: (Long) -> Unit,
    isExpanded: Boolean,
    isToday: Boolean,
    tasks: List<Task>,
    isInMonth: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = if(tasks.isEmpty())
        RoundedCornerShape(12.dp)
    else
        RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(0.9F)
            .clip(shape)
            .clickable { onClick(day.toEpochDay()) }
            .background(if (isExpanded) Color(0x40005500) else Color.Transparent)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${day.dayOfMonth}",
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = if(isInMonth) Color.Black else Color.LightGray,
                modifier = Modifier.weight(1f)
            )
            var dots = ""
            for (task in tasks) {
                // 1 dot for each task in this day
                dots += "·"
            }
            Text(text = dots,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun DaysOfTheWeekInitialsHeader() {
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(32.dp)) {

        for (dayOfWeek in DayOfWeek.entries) {
            val dayName = dayOfWeek.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault())
            Text(
                text = dayName,
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1F)
            )
        }
    }
}
