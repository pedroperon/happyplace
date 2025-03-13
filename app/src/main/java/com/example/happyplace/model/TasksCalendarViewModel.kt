package com.example.happyplace.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.happyplace.LocalTasksList
import com.example.happyplace.Task
import com.example.happyplace.User
import com.example.happyplace.data.TasksListRepository
import com.example.happyplace.utils.startOfDayMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

fun getDefaultUsersList() : List<User> {
    return listOf(
        User.newBuilder().apply {
            name = "Pedro"
            id = 1001
        }.build(),
        User.newBuilder().apply {
            name = "Caro"
            id = 1002
        }.build()
    )
}

data class TasksCalendarUiState(
    val tasks : List<Task> = listOf(),
    val users : List<User> = getDefaultUsersList(),
    val showEditTaskDialog : Boolean = false,
    val taskStagedForEdition : Task? = null,
    val expandedDay : Long? = LocalDate.now().toEpochDay()
)

class TasksCalendarViewModel(
    private val tasksListRepository: TasksListRepository
) : ViewModel() {

    val initialSetupEvent = liveData {
        emit(tasksListRepository.fetchInitialTasksList())
    }

    val tasksListUiState = tasksListRepository.tasksListFlow.asLiveData()

    private val _uiState = MutableStateFlow(TasksCalendarUiState())
    val uiState: StateFlow<TasksCalendarUiState> = _uiState.asStateFlow()

    fun openNewTaskDialog() {
        openEditTaskDialog(null)
    }
    fun openEditTaskDialog(task:Task?) {
        _uiState.update {
            it.copy(
                showEditTaskDialog = true,
                taskStagedForEdition = task
            )
        }
    }
    fun closeEditTaskDialog() {
        _uiState.update {
            it.copy(
                showEditTaskDialog = false,
                taskStagedForEdition = null
            )
        }
    }

    fun setTasksList(retrievedTasksList: LocalTasksList) {
        _uiState.update {
            it.copy(
                tasks = retrievedTasksList.tasksList,
                users = getDefaultUsersList() //retrievedTasksList.usersList
            )
        }
    }

    fun toggleShowDay(epochDay:Long?, allowUnselect:Boolean=true) : Boolean {
        var wasSelected = false
        _uiState.update { currentState ->
            wasSelected = (currentState.expandedDay==epochDay)
            currentState.copy(
                expandedDay = if(wasSelected && allowUnselect) null else epochDay
            )
        }
        return !(wasSelected && allowUnselect)
    }

    fun saveTask(task: Task) {
        viewModelScope.launch {
                tasksListRepository.saveNewTask(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            tasksListRepository.deleteTask(task)
        }
    }

    fun getTasksForMonth(monthOffset: Int): List<Task> {
        val firstDay = LocalDate.now()
            .withDayOfMonth(1)
            .plusMonths(monthOffset.toLong())

        val firstDayOfMonthStartInMillis = firstDay.startOfDayMillis()

        val firstDayNextMonthStartInMillis = firstDay
            .plusMonths(1L)
            .startOfDayMillis()

        return _uiState.value.tasks.filter {
            it.initialDate in firstDayOfMonthStartInMillis..<firstDayNextMonthStartInMillis
        }.sortedBy { it.initialDate }
    }

    fun getTasksForNextDays(days: Long): List<Task> {
        val start = LocalDate.now().startOfDayMillis()
        val end = LocalDate.now().plusDays(days+1).startOfDayMillis()

        return _uiState.value.tasks.filter {
            it.initialDate in start..<end
        }.sortedBy { it.initialDate }
    }
}

class TasksCalendarViewModelFactory(
    private val tasksListRepository : TasksListRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TasksCalendarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TasksCalendarViewModel(tasksListRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}