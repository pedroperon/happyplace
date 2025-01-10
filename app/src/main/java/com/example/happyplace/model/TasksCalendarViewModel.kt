package com.example.happyplace.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.example.happyplace.LocalTasksList
import com.example.happyplace.Task
import com.example.happyplace.data.TasksListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TasksCalendarUiState(
    val tasks : List<Task> = listOf(),
    val showEditTaskDialog : Boolean = false
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
        _uiState.update {
            it.copy(
                showEditTaskDialog = true
            )
        }
    }

    fun setTasksList(retrievedTasksList: LocalTasksList) {
        _uiState.update {
            it.copy(
                tasks = retrievedTasksList.tasksList,
            )
        }
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