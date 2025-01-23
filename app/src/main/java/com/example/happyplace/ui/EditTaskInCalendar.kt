package com.example.happyplace.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.happyplace.R
import com.example.happyplace.Task
import com.example.happyplace.model.EditTaskViewModel
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskPopupDialog(
    onDismissRequest: ()->Unit,
    onClickSave: (Task)->Unit,
    editTaskViewModel: EditTaskViewModel,
    initialEpochDay: Long? = null
    ) {
    val editTaskUiState by editTaskViewModel.uiState.collectAsState()

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
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                )

                OutlinedTextField(
                    //NAME
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
                    modifier = Modifier.fillMaxWidth()
                )

                val initialDateMillis = if(initialEpochDay!=null)
                    LocalDate.ofEpochDay(initialEpochDay).atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
                else
                    System.currentTimeMillis()

                val datePickerState = rememberDatePickerState(
                    initialDisplayMode = DisplayMode.Input,
                    initialSelectedDateMillis = initialDateMillis
                )
                DatePicker(
                    state = datePickerState,
                    title = null,//{Text(text = "title")},
                    headline = null,//{ Text(text = "headline")},
                    showModeToggle = false)

                OutlinedTextField(
                    //DETAIL
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
                                .setInitialDate(datePickerState.selectedDateMillis!!) // start of the day
                                .build()
                            )

                            onDismissRequest()
                        },
                        enabled = (
                                editTaskUiState.taskBeingEdited.name.isNotEmpty() &&
                                datePickerState.selectedDateMillis!=null
                                )
                    ) {
                        Text(text = stringResource(R.string.done))
                    }
                }
            }
        }
    }
}