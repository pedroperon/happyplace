package com.example.happyplace.model

import androidx.lifecycle.ViewModel
import com.example.happyplace.Periodicity
import com.example.happyplace.Periodicity.IntervalType
import com.example.happyplace.Task
import com.example.happyplace.Task.TaskType
import com.example.happyplace.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val INVALID_DATE = -10000L

data class EditTaskUiState(
    val taskBeingEdited : Task,
    val userDropDownExpanded : Boolean = false,
    val taskTypeDropDownExpanded : Boolean = false
)

class EditTaskViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        EditTaskUiState(getNewBlankTask())
    )
    val uiState: StateFlow<EditTaskUiState> = _uiState.asStateFlow()

    fun updateName(newName: String) {
        _uiState.update { currentState ->
            currentState.copy(
                taskBeingEdited = currentState.taskBeingEdited
                    .toBuilder()
                    .setName(newName)
                    .build()
            )
        }
    }

    fun setTaskBeingEdited(originalTask: Task?) {
        _uiState.update {
            it.copy(taskBeingEdited = originalTask ?: getNewBlankTask())
        }
    }

    fun updateDetails(newDetails: String) {
        _uiState.update { currentState ->
            currentState.copy(
                taskBeingEdited = currentState.taskBeingEdited
                    .toBuilder()
                    .setDetails(newDetails)
                    .build()
            )
        }
    }

    fun toggleTypeSelectionExpanded(expanded: Boolean? = null) {
        _uiState.update { currentState ->
            currentState.copy(
                taskTypeDropDownExpanded = expanded ?: !currentState.taskTypeDropDownExpanded
            )
        }
    }

    fun updateType(type: TaskType) {
        _uiState.update { currentState ->
            currentState.copy(
                taskBeingEdited = currentState.taskBeingEdited
                    .toBuilder()
                    .setType(type)
                    .build()
            )
        }
    }

    fun toggleUserSelectionExpanded(expanded: Boolean? = null) {
        _uiState.update { currentState ->
            currentState.copy(
                userDropDownExpanded = expanded ?: !currentState.userDropDownExpanded
            )
        }
    }

    fun updateOwner(user: User) {
        _uiState.update { currentState ->
            currentState.copy(
                taskBeingEdited = currentState.taskBeingEdited
                    .toBuilder()
                    .setTaskOwner(user)
                    .build()
            )
        }
    }

    fun updatePeriodicityIntervalType(newIntervalType : IntervalType) {
        if(newIntervalType == IntervalType.UNRECOGNIZED)
            return

        _uiState.update { currentState ->
            val currentNumberIntervals = currentState.taskBeingEdited.periodicity?.numberOfIntervals ?: 0

            currentState.copy(
                taskBeingEdited = currentState.taskBeingEdited
                    .toBuilder().apply {
                        periodicity = Periodicity.newBuilder()
                            .setIntervalType(newIntervalType)
                            .setNumberOfIntervals(currentNumberIntervals)
                            .build()
                }
                    .build()
            )
        }
    }

    fun updatePeriodicityNumber(newNumber: String) {
        if ((newNumber.isNotEmpty() && newNumber.toIntOrNull()==null) //NaN
            || newNumber.length > 2
        )
            return

        // newNumber either empty or non negative int
        _uiState.update { currentState ->
            val currentIntervalType = currentState.taskBeingEdited.periodicity?.intervalType
                ?: IntervalType.DAY

            currentState.copy(
                taskBeingEdited = currentState.taskBeingEdited
                    .toBuilder().apply {
                        periodicity = Periodicity.newBuilder()
                            .setNumberOfIntervals(newNumber.ifEmpty{"0"}.toInt())
                            .setIntervalType(currentIntervalType)
                            .build()
                    }
                    .build()
            )
        }
    }

    fun resetPeriodicity() {
        _uiState.update { currentState ->
            currentState.copy(
                taskBeingEdited = currentState.taskBeingEdited.toBuilder().apply {
                    periodicity = Periodicity.newBuilder()
                        .setNumberOfIntervals(0)
                        .setIntervalTypeValue(0)
                        .build()
                }
                    .build()
            )
        }
    }
}

private fun getNewBlankTask(): Task {
    return Task.newBuilder().build()
}