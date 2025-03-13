package com.example.happyplace.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.happyplace.R
import com.example.happyplace.Task
import com.example.happyplace.Task.TaskType
import com.example.happyplace.User
import com.example.happyplace.model.EditTaskViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

enum class ShowPickerState {
    DATE,
    TIME,
    NONE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskPopupDialog(
    onDismissRequest: () -> Unit,
    onClickSave: (Task) -> Unit,
    editTaskViewModel: EditTaskViewModel,
    initialEpochDay: Long? = null,
    users: List<User>
) {
    val editTaskUiState by editTaskViewModel.uiState.collectAsState()
    var showPicker by rememberSaveable { mutableStateOf(ShowPickerState.NONE) }

    val initialDateMillis =
        ((initialEpochDay?.let { LocalDate.ofEpochDay(it) }) ?: LocalDate.now())
            .atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()

    val datePickerState = rememberDatePickerState(
        initialDisplayMode = DisplayMode.Picker,
        initialSelectedDateMillis = initialDateMillis)
    val datePickedUTC = LocalDate.ofInstant(
        Instant.ofEpochMilli(datePickerState.selectedDateMillis!!), ZoneId.of("UTC"))

    val timePickerState = rememberTimePickerState(is24Hour = true)

    val onDismissPicker = { showPicker=ShowPickerState.NONE }

    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .background(Color.White)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // TITLE
                Text(
                    text = stringResource(R.string.new_task),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )

                //NAME
                OutlinedTextField(
                    value = editTaskUiState.taskBeingEdited.name,
                    onValueChange = { editTaskViewModel.updateName(it) },
                    enabled = true,
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    label = { Text(text = stringResource(R.string.task)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged {
                            if (!it.isFocused)
                                editTaskViewModel.updateName(editTaskUiState.taskBeingEdited.name.trim())
                        }
                )

                // TASK TYPE
                val expandTypeDD = editTaskUiState.taskTypeDropDownExpanded
                val dismissTypeDD = { editTaskViewModel.toggleTypeSelectionExpanded(false) }

                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable(onClick = { editTaskViewModel.toggleTypeSelectionExpanded() })
                    ) {
                        val title = stringResource(R.string.type)
                        Text(text = title)
                        val currentOptionName = getTaskTypeName(editTaskUiState.taskBeingEdited.type)
                        Text(text = currentOptionName)
                        Image(
                            painter = painterResource(R.drawable.baseline_arrow_right_24),
                            contentDescription = stringResource(R.string.expand_list),
                            modifier = Modifier.rotate(if (expandTypeDD) 270F else 90F)
                        )

                        DropdownMenu(
                            expanded = expandTypeDD,
                            onDismissRequest = dismissTypeDD
                        ) {
                            TaskType.entries
                                .subList(0, TaskType.entries.size - 1) // excludes automatically generated UNRECOGNIZED
                                .forEach {
                                DropdownMenuItem(
                                    text = {
                                        Text(text = getTaskTypeName(it))
                                    },
                                    onClick = {
                                        editTaskViewModel.updateType(it)
                                        dismissTypeDD()
                                    }
                                )
                            }
                        }
                    }
                }

                // DATE
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showPicker = ShowPickerState.DATE }) {
                    Icon(Icons.Filled.DateRange, null)
                    Text(
                        text = datePickedUTC.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)) +
                                " (${
                                    datePickedUTC.dayOfWeek.getDisplayName(
                                        TextStyle.FULL,
                                        Locale.getDefault()
                                    )
                                })",
                        modifier = Modifier.padding(8.dp)
                    )
                }

                // TIME
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showPicker = ShowPickerState.TIME }) {
                    Icon(Icons.Filled.CheckCircle, null)
                    Text(
                        text = "%02d".format(timePickerState.hour) + ":%02d".format(timePickerState.minute),
                        modifier = Modifier.padding(8.dp)
                    )
                }

                // RECURRENT
                Column {
                    var checkedAsRecurrent by rememberSaveable { mutableStateOf(false) }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = checkedAsRecurrent,
                            onCheckedChange = {
                                editTaskViewModel.resetPeriodicity()
                                checkedAsRecurrent = !checkedAsRecurrent
                            })
                        Text(text = "Repeats")
                        if (checkedAsRecurrent) {
                            Text(text = " every ")
                            TextField(
                                value = toStringEmptyIfZero(editTaskUiState.taskBeingEdited.periodicity.numberOfIntervals),
                                onValueChange = { editTaskViewModel.updatePeriodicityNumber(it) },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    //imeAction = ImeAction.Next,
                                ),
                                modifier = Modifier.width(60.dp)
                            )
                            Text(text = " days")
                        }
                    }
                }

                // TASK OWNER
                val expanded = editTaskUiState.userDropDownExpanded
                val dismiss = { editTaskViewModel.toggleUserSelectionExpanded(false) }
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable(onClick = { editTaskViewModel.toggleUserSelectionExpanded() })
                    ) {
                        val title = stringResource(R.string.task_owner)
                        Text(text = title)
                        val currentOptionName = editTaskUiState.taskBeingEdited.taskOwner.name
                        Text(text = currentOptionName ?: "")
                        Image(
                            painter = painterResource(R.drawable.baseline_arrow_right_24),
                            contentDescription = stringResource(R.string.expand_list),
                            modifier = Modifier.rotate(if (expanded) 270F else 90F)
                        )

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = dismiss
                        ) {
                            users.forEach {
                                DropdownMenuItem(
                                    text = {
                                        Text(text = it.name)
                                    },
                                    onClick = {
                                        editTaskViewModel.updateOwner(it)
                                        dismiss()
                                    }
                                )
                            }
                        }
                    }
                }

                //DETAIL
                OutlinedTextField(
                    value = editTaskUiState.taskBeingEdited.details,
                    onValueChange = { editTaskViewModel.updateDetails(it) },
                    enabled = true,
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    label = { Text(text = stringResource(R.string.details)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // BUTTONS CANCEL / DONE
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.weight(1.0F))
                    Text(text = stringResource(R.string.cancel),
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clickable { onDismissRequest() })
                    Button(
                        onClick = {
                            onClickSave(
                                editTaskUiState.taskBeingEdited
                                    .toBuilder()
                                    .setInitialDate(
                                        datePickedUTC.atStartOfDay(ZoneId.systemDefault())
                                            .toInstant().toEpochMilli()
                                                + (timePickerState.hour * 60 + timePickerState.minute) * 60 * 1000 // hours and minutes in milliseconds
                                    )
                                    .build()
                            )
                            onDismissRequest()
                        },
                        enabled = (
                                editTaskUiState.taskBeingEdited.name.isNotEmpty() &&
                                        editTaskUiState.taskBeingEdited.taskOwner.name.isNotEmpty() &&
                                        datePickerState.selectedDateMillis != null
                                )
                    ) {
                        Text(text = stringResource(R.string.done))
                    }
                }
            }
        }
    }

    if(showPicker==ShowPickerState.DATE) {
        PickerPopup(onDismissPicker){
            DatePicker(
                state = datePickerState,
                title = null,
                headline = null,
                showModeToggle = false,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
    if(showPicker==ShowPickerState.TIME) {
        PickerPopup(onDismissPicker) {
            TimePicker(
                state = timePickerState,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

@Composable
private fun PickerPopup(
    onDismissPicker: () -> Unit,
    content: @Composable (()->Unit)
) {
    Dialog(
        onDismissRequest = onDismissPicker,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(
                horizontalAlignment = Alignment.End,
            ) {
                content()

                Button(onClick = onDismissPicker, modifier = Modifier.padding(12.dp)) {
                    Text(text = stringResource(R.string.done))
                }

            }
        }
    }
}

@Composable
fun getTaskTypeName(type:TaskType): String {
    return (taskTypeNameIds[type.name]
        ?.let { id -> stringResource(id) }
        ?: type.name.lowercase())
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}
private val taskTypeNameIds = mapOf(
    "UNDEFINED" to R.string.undefined,
    "CLEANING" to R.string.cleaning,
    "MAINTENANCE" to R.string.maintenance,
    "SHOPPING" to R.string.shopping,
    "COOKING" to R.string.cooking
)

@Preview
@Composable
fun EditTaskPopupDialogPreview() {
    EditTaskPopupDialog(
        onDismissRequest = {},
        onClickSave = {},
        editTaskViewModel = EditTaskViewModel(),
        users = emptyList()
    )
}
