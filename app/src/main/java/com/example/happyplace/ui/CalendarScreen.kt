package com.example.happyplace.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animate
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
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
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.happyplace.utils.containsDateTimeInMillis
import com.example.happyplace.utils.firstSundayAfterCurrentMonth
import com.example.happyplace.utils.lastMondayBeforeCurrentMonth
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.round

const val MAX_MONTHS : Int = 1200 // 100 years
const val INITIAL_MONTH_OFFSET = MAX_MONTHS/4

@OptIn(ExperimentalFoundationApi::class)
suspend fun PagerState.customAnimateScrollToPage(page: Int) {
    scroll {
        // Update the target page
        updateTargetPage(page)

        val distance = (page-currentPage) * layoutInfo.pageSize.toFloat()
        var previousValue = 0.0f
        animate(
            0f,
            distance,
        ) { currentValue, _ ->
            previousValue += scrollBy(currentValue - previousValue)
        }
    }
}

@Composable
fun CalendarScreen(
    tasksCalendarViewModel: TasksCalendarViewModel,
    editTaskViewModel: EditTaskViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by tasksCalendarViewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(initialPage = INITIAL_MONTH_OFFSET){ MAX_MONTHS }

    Box(modifier = modifier.fillMaxSize()) {
        val coroutineScope = rememberCoroutineScope()

        val isDragged by pagerState.interactionSource.collectIsDraggedAsState() //.collectIsDraggedAsState()

        HorizontalPager(state = pagerState) { page ->
            val monthOffset = page - INITIAL_MONTH_OFFSET
            val tasks = tasksCalendarViewModel.getTasksForMonth(monthOffset)

            Column {
                DaysOfTheWeekInitialsHeader()
                Box(modifier = modifier
                    .fillMaxHeight()
                    .background(Color.White)
                    .clickable { tasksCalendarViewModel.toggleShowDay(null) }
                ) {
                    MonthBox(
                        monthOffset = monthOffset,
                        onClickDay = {
                            val selected = tasksCalendarViewModel.toggleShowDay(it)

                            // if day in another (adjacent) month, scroll to correct month
                            if(selected) {
                                val delta = LocalDate.ofEpochDay(it!!).monthValue -
                                        LocalDate.now().plusMonths(monthOffset.toLong()).monthValue
                                coroutineScope.launch {
                                    pagerState.customAnimateScrollToPage(pagerState.currentPage + delta)
                                }
                            }
                                     },
                        expandedDay = uiState.expandedDay,
                        tasksInMonth = tasks,
                        onClickTask = {tasksCalendarViewModel.deleteTask(it)},
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
            onClickSave = {
                tasksCalendarViewModel.saveTask(it)
                askPermissionForNotifications()
                          },
            editTaskViewModel = editTaskViewModel,
            users = uiState.users
        )
    }
}

fun askPermissionForNotifications() {
    //TODO("Not yet implemented")
}

@Composable
fun MonthBox(
    monthOffset: Int,
    onClickDay: (Long?) -> Unit,
    expandedDay: Long?,
    tasksInMonth: List<Task>,
    onClickTask:(Task)->Unit,
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
            color = Color(0xFFF2F2F2),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(36.dp).fillMaxSize()
        )

        Column(modifier = Modifier.fillMaxSize()) {
            repeat(dayRowsTotal) { weekCount ->
                WeekBox(
                    startDay = startDay.plusWeeks(weekCount.toLong()),
                    onClickDay = onClickDay,
                    expandedDay = expandedDay,
                    firstDayOfMonth = firstDayOfMonth,
                    today = today,
                    tasksInMonth = tasksInMonth,
                    onClickTask = onClickTask
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
    tasksInMonth: List<Task>,
    onClickTask:(Task)->Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row {
            repeat(7) { dayCount ->
                val day = startDay.plusDays(dayCount.toLong())
                DayBox(
                    day = day,
                    onClick = onClickDay,
                    isExpanded = (day.toEpochDay() == expandedDay),
                    isToday = (day == today),
                    tasksOfTheDay = tasksInMonth.filter { day.containsDateTimeInMillis(it.initialDate) },
                    isInMonth = (firstDayOfMonth == day.withDayOfMonth(1)),
                    modifier = Modifier.weight(1F)
                )
            }
        }
        DayTasksBox(
            epochDay = expandedDay,
            weekStart = startDay,
            tasks = tasksInMonth,
            onClickTask = onClickTask
        )
    }
}

@Composable
fun DayTasksBox(epochDay: Long?,
                weekStart: LocalDate,
                tasks: List<Task>,
                onClickTask: (Task)->Unit) {

    Column(modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
        .background(Color(0x40005500))
        .animateContentSize()) {
        if (epochDay != null &&
            (epochDay - weekStart.toEpochDay()) in 0..<7
        ) {
            val dayTasks = tasks.filter {
                LocalDate.ofEpochDay(epochDay).containsDateTimeInMillis(it.initialDate)
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
                    Column(modifier = Modifier.verticalScroll(rememberScrollState(0))) {
                        for (task in dayTasks) {
                            TaskBox(task, onDoubleClick = onClickTask)
                        }
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskBox(task:Task, onClick:((Task)->Unit)={}, onDoubleClick:((Task)->Unit)={}) {
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            //.clickable { onClick(task) }
            .combinedClickable(onDoubleClick = { onDoubleClick(task) }) { onClick(task) }
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ) {
        val color = Color.DarkGray //TODO: make this dependent on task type
        val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(task.initialDate), ZoneId.systemDefault())

        Icon(painterResource(R.drawable.baseline_arrow_right_24), null, tint = color)
        Text(
            text = dateTime.format(DateTimeFormatter.ofPattern("HH:mm")),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Text(
            text = task.name,
            color = color,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        if(task.taskOwner.name.isNotEmpty())
            Text(
                text = "(${task.taskOwner.name})",
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

    }
}

@Composable
private fun DayBox (
    day: LocalDate,
    onClick: (Long) -> Unit,
    isExpanded: Boolean,
    isToday: Boolean,
    tasksOfTheDay: List<Task>,
    isInMonth: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = if(tasksOfTheDay.isEmpty())
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
            for (task in tasksOfTheDay) {
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
