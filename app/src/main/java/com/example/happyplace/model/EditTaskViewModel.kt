package com.example.happyplace.model

import androidx.lifecycle.ViewModel
import com.example.happyplace.ShoppingListItem
import com.example.happyplace.Task
import com.example.happyplace.User
import com.example.happyplace.task
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
}

private fun getNewBlankTask(): Task {
    return Task.newBuilder().build()
}